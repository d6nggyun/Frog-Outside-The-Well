package com._oormthon.seasonthon.domain.ai.service;

import com._oormthon.seasonthon.domain.ai.client.GeminiApiClient;
import com._oormthon.seasonthon.domain.ai.entity.UserConversation;
import com._oormthon.seasonthon.domain.ai.enums.ConversationState;
import com._oormthon.seasonthon.domain.ai.repository.UserConversationRepository;
import com._oormthon.seasonthon.domain.ai.scripts.ChatbotScript;
import com._oormthon.seasonthon.domain.step.domain.TodoStep;
import com._oormthon.seasonthon.domain.step.repository.TodoStepRepository;
import com._oormthon.seasonthon.domain.todo.domain.Todo;
import com._oormthon.seasonthon.domain.todo.dto.res.TodoStepResponse;
import com._oormthon.seasonthon.domain.todo.repository.TodoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class GeminiChatService {

    private final UserConversationRepository conversationRepo;
    private final GeminiApiClient geminiApiClient;
    private final TodoStepRepository todoStepRepository;
    private final TodoRepository todoRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final Pattern STEPS_JSON_PATTERN = Pattern.compile("\\{.*\"steps\"\\s*:\\s*\\[.*\\].*\\}",
            Pattern.DOTALL);

    public GeminiChatService(
            UserConversationRepository conversationRepo,
            GeminiApiClient geminiApiClient,
            TodoStepRepository todoStepRepository,
            TodoRepository todoRepository) {
        this.conversationRepo = conversationRepo;
        this.geminiApiClient = geminiApiClient;
        this.todoStepRepository = todoStepRepository;
        this.todoRepository = todoRepository;
    }

    /**
     * 사용자 입력 메시지 처리 (Gemini 스트리밍 포함)
     */
    public Flux<String> handleUserMessageStream(Long userId, String userMessageJson) {
        String userMessage = extractMessage(userMessageJson);
        log.info("🗣 사용자 입력: {}", userMessage);

        return Mono.fromCallable(() -> processUserMessage(userId, userMessage))
                .flatMapMany(result -> {
                    if (result.isStreaming()) {
                        // Gemini 스트림 응답 처리
                        return geminiApiClient.generateStream(result.prompt())
                                .flatMap(chunk -> trySaveTodoAndStepsReactive(userId, chunk, result.stepIndex())
                                        .thenReturn(chunk))
                                .concatWith(Mono.defer(() -> {
                                    if (result.stepIndex() == 1) {
                                        String content = conversationRepo.findByUserId(userId)
                                                .map(UserConversation::getContent)
                                                .orElse("");
                                        String title = conversationRepo.findByUserId(userId)
                                                .map(UserConversation::getTitle)
                                                .orElse("");
                                        return Mono.just(
                                                ChatbotScript.askStartDate(content, title));
                                    } else if (result.stepIndex() == 2) {
                                        return Mono.just("✅ 계획 저장 완료");
                                    } else {
                                        return Mono.empty();
                                    }
                                }));
                    } else {
                        // 일반 메시지 응답
                        return Flux.just(result.response());
                    }
                })
                .onErrorResume(e -> {
                    log.error("💥 스트림 처리 중 오류", e);
                    return Flux.just("문제가 발생했어 😢 다시 시도해줄래?");
                });
    }

    /**
     * JSON에서 "message" 필드 추출
     */
    private String extractMessage(String userMessageJson) {
        try {
            JsonNode node = objectMapper.readTree(userMessageJson);
            return node.has("message") ? node.get("message").asText().trim() : "";
        } catch (Exception e) {
            log.error("💥 JSON 파싱 실패: {}", userMessageJson, e);
            return "";
        }
    }

    /**
     * 단계별로 다른 저장 로직 수행 (Reactive Wrapper)
     */
    private Mono<Void> trySaveTodoAndStepsReactive(Long userId, String dataChunk, int stepIndex) {
        return switch (stepIndex) {
            case 1 -> savePlanDescription(userId, dataChunk);
            case 2 -> saveTodoAndSteps(userId, dataChunk);
            default -> Mono.empty();
        };
    }

    /**
     * Step 1: 학습 목표 설명 저장
     */
    private Mono<Void> savePlanDescription(Long userId, String dataChunk) {
        String description = String.join("", dataChunk).replaceAll("```", "").trim();
        return Mono.fromCallable(() -> {
            try {
                UserConversation convo = conversationRepo.findByUserId(userId).orElse(null);
                if (convo == null)
                    return null;

                convo.setContent(description);
                convo.setPlanSaved(false);
                conversationRepo.save(convo);

                log.info("📘 계획 설명 저장 완료 (userId={}): {}", userId, dataChunk);
            } catch (Exception e) {
                log.debug("⚠️ Step1 저장 실패: {}", e.getMessage());
            }
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * Step 2: 실제 Todo + Step 생성 및 저장
     */
    private Mono<Void> saveTodoAndSteps(Long userId, String dataChunk) {
        if (dataChunk == null || !dataChunk.contains("{") || !dataChunk.contains("steps")) {
            return Mono.empty();
        }

        Matcher matcher = STEPS_JSON_PATTERN.matcher(dataChunk);
        if (!matcher.find())
            return Mono.empty();

        String jsonBlock = matcher.group();

        return Mono.fromCallable(() -> {
            try {
                TodoStepResponse parsed = objectMapper.readValue(jsonBlock, TodoStepResponse.class);

                UserConversation convo = conversationRepo.findByUserId(userId).orElse(null);
                if (convo == null || convo.isPlanSaved()) {
                    return null;
                }

                Todo todo = Todo.builder()
                        .userId(userId)
                        .title(convo.getTitle())
                        .content(convo.getContent())
                        .startDate(convo.getStartDate())
                        .endDate(convo.getEndDate())
                        .progress(0)
                        .expectedDays(DayConverter.parseDays(convo.getStudyDays()))
                        .build();
                todoRepository.save(todo);

                List<TodoStep> todoSteps = parsed.steps().stream()
                        .map(step -> TodoStep.builder()
                                .todoId(todo.getId())
                                .userId(userId)
                                .stepDate(step.stepDate())
                                .description(step.description())
                                .isCompleted(step.isCompleted())
                                .build())
                        .toList();

                todoStepRepository.saveAll(todoSteps);
                convo.setPlanSaved(true);
                conversationRepo.save(convo);

                log.info("💾 Todo({}) 및 {}개 Step 저장 완료 (userId={})",
                        todo.getTitle(), todoSteps.size(), userId);
            } catch (Exception e) {
                log.debug("⚠️ Step2 JSON 저장 실패: {}", e.getMessage());
            }
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * 사용자 입력 메시지 처리 및 다음 상태 결정
     */
    @Transactional
    protected MessageResult processUserMessage(Long userId, String userMessage) {
        UserConversation convo = conversationRepo.findByUserId(userId)
                .orElseGet(() -> {
                    UserConversation uc = new UserConversation();
                    uc.setUserId(userId);
                    uc.setState(ConversationState.INTRO);
                    conversationRepo.save(uc);
                    log.info("🆕 새 대화 생성 (userId={})", userId);
                    return uc;
                });

        String response = null;
        boolean streaming = false;
        String prompt = null;
        int stepIndex = 0;

        try {
            switch (convo.getState()) {
                case INTRO -> {
                    response = ChatbotScript.intro();
                    convo.setState(ConversationState.ASK_READY);
                }
                case ASK_READY -> {
                    response = ChatbotScript.readyResponse(userMessage);
                    convo.setState(ConversationState.ASK_NAME);
                }
                case ASK_NAME -> {
                    convo.setUserName(userMessage.trim());
                    response = ChatbotScript.askAge(convo.getUserName());
                    convo.setState(ConversationState.ASK_AGE);
                }
                case ASK_AGE -> {
                    try {
                        int age = Integer.parseInt(userMessage.trim());
                        convo.setUserAge(age);
                        response = ChatbotScript.ageResponse(age, convo.getUserName());
                        convo.setState(ConversationState.ASK_TASK);
                    } catch (NumberFormatException e) {
                        response = "숫자로 나이를 입력해줘! 예: 16";
                    }
                }
                case ASK_TASK -> {
                    convo.setTitle(userMessage.trim());
                    prompt = ChatbotScript.planDetail(userMessage.trim());
                    stepIndex = 1;
                    streaming = true;
                    convo.setState(ConversationState.ASK_START_DATE);
                }
                case ASK_START_DATE -> {
                    try {
                        LocalDate start = LocalDate.parse(userMessage.trim(), dateFormatter);
                        convo.setStartDate(start);
                        response = ChatbotScript.askEndDate(start);
                        convo.setState(ConversationState.ASK_END_DATE);
                    } catch (Exception e) {
                        response = "날짜는 'yyyy-MM-dd' 형식으로 입력해줘!";
                    }
                }
                case ASK_END_DATE -> {
                    try {
                        LocalDate end = LocalDate.parse(userMessage.trim(), dateFormatter);
                        convo.setEndDate(end);
                        response = ChatbotScript.askStudyDays(convo.getStartDate(), convo.getEndDate());
                        convo.setState(ConversationState.ASK_DAYS);
                    } catch (Exception e) {
                        response = "날짜는 'yyyy-MM-dd' 형식으로 입력해줘!";
                    }
                }
                case ASK_DAYS -> {
                    convo.setStudyDays(userMessage.trim());
                    response = "좋아! 하루 공부 시간을 (분 단위로) 알려줘.";
                    convo.setState(ConversationState.ASK_TIME_PER_DAY);
                }
                case ASK_TIME_PER_DAY -> {
                    try {
                        int minutes = Integer.parseInt(userMessage.trim());
                        convo.setDailyMinutes(minutes);
                        prompt = ChatbotScript.planPrompt(convo);
                        stepIndex = 2;
                        streaming = true;
                        convo.setState(ConversationState.CONFIRM_PLAN);
                    } catch (NumberFormatException e) {
                        response = "공부 시간은 숫자로 입력해줘! 예: 90";
                    }
                }
                case CONFIRM_PLAN -> {
                    if (userMessage.contains("좋아") || userMessage.contains("응")) {
                        response = "좋아! 🎉 이 계획으로 진행할게. 화이팅 💪";
                        convo.setState(ConversationState.FINISHED);
                    } else if (userMessage.contains("아니") || userMessage.contains("수정")) {
                        response = "괜찮아 😊 어떤 점을 수정할까?";
                        convo.setState(ConversationState.ASK_TASK);
                    } else {
                        response = "이 계획으로 진행할까? (좋아 / 수정)";
                    }
                }
                case FINISHED -> {
                    if (userMessage.contains("새로운 계획")) {
                        convo.setState(ConversationState.INTRO);
                        response = "좋아! 🐸 새로운 공부 계획을 세워보자!";
                    } else {
                        response = "이미 계획이 완성됐어 🎯 '새로운 계획'이라고 말해줘!";
                    }
                }
                default -> response = "무슨 말인지 모르겠어 😅 다시 말해줄래?";
            }

            conversationRepo.save(convo);
        } catch (Exception e) {
            log.error("💥 메시지 처리 오류", e);
            response = "오류가 발생했어 😢 다시 시도해줘.";
        }

        return new MessageResult(response, prompt, streaming, stepIndex);
    }

    /**
     * 내부 메시지 결과 DTO
     */
    private record MessageResult(String response, String prompt, boolean isStreaming, int stepIndex) {
    }
}

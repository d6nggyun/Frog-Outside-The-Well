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

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Service
public class GeminiChatService {

    private final UserConversationRepository conversationRepo;
    private final GeminiApiClient geminiApiClient;
    private final TodoStepRepository todoStepRepository;
    private final TodoRepository todoRepository;
    // private final TodoQueryService todoQueryService;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final ObjectMapper objectMapper = new ObjectMapper(); // ✅ JSON 파싱용

    private static final Pattern STEPS_JSON_PATTERN = Pattern.compile("\\{.*\"steps\"\\s*:\\s*\\[.*\\].*\\}",
            Pattern.DOTALL);

    public GeminiChatService(UserConversationRepository repo, GeminiApiClient client,
            TodoStepRepository todoStepRepository,
            TodoRepository todoRepository) {
        this.conversationRepo = repo;
        this.geminiApiClient = client;
        this.todoStepRepository = todoStepRepository;
        this.todoRepository = todoRepository;
    }

    /**
     * 사용자 메시지를 단계별로 처리하고 필요 시 Gemini SSE 응답 Flux로 반환
     */
    public Flux<String> handleUserMessageStream(Long userId, String userMessageJson) {
        String userMessage = extractMessage(userMessageJson);
        log.info("🆕 사용자 message userMessageJson={}", userMessageJson);

        return Mono.defer(() -> Mono.fromCallable(() -> processUserMessage(userId, userMessage)))
                .flatMapMany(result -> {
                    if (result.isStreaming()) {
                        // ✅ SSE 기반 Gemini 스트림 요청
                        return geminiApiClient.generateStream(result.prompt())
                                .doOnNext(chunk -> trySaveTodoAndSteps(userId, chunk))
                                .concatWith(Flux.just("✅ 계획 저장 완료"))
                                .delayElements(Duration.ofMillis(80));

                    } else {
                        return Flux.just(result.response());
                    }
                })
                .onErrorResume(e -> Flux.just("미안해 😢 잠시 문제가 생겼어. 다시 시도해줄래?"));
    }

    private String extractMessage(String userMessageJson) {
        try {
            JsonNode node = objectMapper.readTree(userMessageJson);
            return node.has("message") ? node.get("message").asText().trim() : "";
        } catch (Exception e) {
            log.error("💥 userMessage JSON 파싱 실패: {}", userMessageJson, e);
            return "";
        }
    }

    /**
     * SSE 스트림 내 JSON 블록을 감지하고 TodoStep 저장
     */
    @Transactional
    protected void trySaveTodoAndSteps(Long userId, String dataChunk) {
        try {
            if (!dataChunk.contains("{") || !dataChunk.contains("steps"))
                return;

            // JSON 정리 및 파싱
            String cleaned = dataChunk
                    .replaceAll("(?s)```json", "")
                    .replaceAll("(?s)```", "")
                    .trim();

            TodoStepResponse parsed = objectMapper.readValue(cleaned, TodoStepResponse.class);

            // 현재 대화 상태 확인
            UserConversation convo = conversationRepo.findByUserId(userId).orElse(null);
            if (convo == null || convo.getTitle() == null)
                return;

            // ✅ Todo 생성
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

            // ✅ TodoStep 생성 및 저장
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

            log.info("💾 Todo({}) 및 {}개의 TodoStep 저장 완료 (userId={})",
                    todo.getTitle(), todoSteps.size(), userId);

        } catch (Exception e) {
            log.debug("⚠️ JSON chunk는 계획 JSON이 아님, skip: {}", e.getMessage());
        }
    }

    /**
     * DB 트랜잭션 내에서 사용자 상태 업데이트 및 다음 프롬프트 생성
     */
    @Transactional
    protected MessageResult processUserMessage(Long userId, String userMessage) {
        UserConversation convo = conversationRepo.findByUserId(userId)
                .orElseGet(() -> {
                    UserConversation uc = new UserConversation();
                    uc.setUserId(userId);
                    uc.setState(ConversationState.INTRO);
                    conversationRepo.save(uc);
                    log.info("🆕 새 사용자 대화 생성 userId={}", userId);
                    return uc;
                });

        String response = null;
        boolean streaming = false;
        String prompt = null;

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
                    geminiApiClient.generateStream(prompt)
                            .collectList()
                            .map(chunks -> String.join("", chunks))
                            .map(text -> text.replaceAll("```", "").trim())
                            .subscribe(description -> {
                                convo.setContent(description);
                            });
                    response = ChatbotScript.askStartDate(convo.getContent(), convo.getTitle());
                    convo.setState(ConversationState.ASK_START_DATE);
                }
                case ASK_START_DATE -> {
                    try {
                        LocalDate start = LocalDate.parse(userMessage.trim(), dateFormatter);
                        convo.setStartDate(start);
                        response = ChatbotScript.askEndDate(start);
                        convo.setState(ConversationState.ASK_END_DATE);
                    } catch (Exception e) {
                        response = "날짜는 'yyyy-MM-dd' 형식으로 입력해줘! 예: 2025-11-01";
                    }
                }
                case ASK_END_DATE -> {
                    try {
                        LocalDate end = LocalDate.parse(userMessage.trim(), dateFormatter);
                        convo.setEndDate(end);
                        response = ChatbotScript.askStudyDays(convo.getStartDate(), convo.getEndDate());
                        convo.setState(ConversationState.ASK_DAYS);
                    } catch (Exception e) {
                        response = "날짜는 'yyyy-MM-dd' 형식으로 입력해줘! 예: 2025-11-15";
                    }
                }
                case ASK_DAYS -> {
                    convo.setStudyDays(userMessage.trim());
                    response = "좋아! 이제 하루 공부 시간을 알려줘 (분 단위로 입력)";
                    convo.setState(ConversationState.ASK_TIME_PER_DAY);
                }
                case ASK_TIME_PER_DAY -> {
                    try {
                        int minutes = Integer.parseInt(userMessage.trim());
                        convo.setDailyMinutes(minutes);
                        prompt = ChatbotScript.planPrompt(convo);
                        streaming = true; // ✅ Gemini SSE 호출 준비 완료
                        convo.setState(ConversationState.CONFIRM_PLAN);
                    } catch (NumberFormatException e) {
                        response = "공부 시간은 숫자로 입력해줘! 예: 90";
                    }
                }
                case CONFIRM_PLAN -> {
                    if (userMessage.contains("좋아") || userMessage.contains("응") || userMessage.contains("ㅇㅇ")) {
                        response = "좋아! 🎉 그럼 이 계획으로 진행할게. 앞으로 화이팅이야 💪";
                        convo.setState(ConversationState.FINISHED);
                    } else if (userMessage.contains("아니") || userMessage.contains("수정")) {
                        response = "괜찮아 😊 어떤 점을 바꿔볼까?";
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
                        response = "이미 계획이 완성됐어 🎯 새로운 계획을 세우려면 '새로운 계획'이라고 말해줘!";
                    }
                }
                default -> response = "무슨 말인지 모르겠어 😅 다시 한 번 말해줄래?";
            }

            conversationRepo.save(convo);
        } catch (Exception e) {
            log.error("💥 Error processing user message: {}", e.getMessage(), e);
            response = "오류가 발생했어 😢 잠시 후 다시 시도해줘.";
        }

        return new MessageResult(response, prompt, streaming);
    }

    /**
     * 내부 응답 모델 (Flux 전송용)
     */
    private record MessageResult(String response, String prompt, boolean isStreaming) {
    }
}

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
import com._oormthon.seasonthon.domain.todo.enums.Day;
import com._oormthon.seasonthon.domain.todo.repository.TodoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiChatService {

    private final UserConversationRepository conversationRepo;
    private final GeminiApiClient geminiApiClient;
    private final TodoStepRepository todoStepRepository;
    private final TodoRepository todoRepository;
    private final ObjectMapper objectMapper;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final Pattern STEPS_JSON_PATTERN = Pattern.compile("\\{.*\"steps\"\\s*:\\s*\\[.*\\].*\\}",
            Pattern.DOTALL);

    /**
     * 사용자 메시지 처리 (Gemini 스트리밍 포함)
     */
    public Flux<String> handleUserMessageStream(Long userId, String userMessageJson) {
        String userMessage = extractMessage(userMessageJson);
        log.info("🗣 사용자 입력: {}", userMessage);

        return Mono.fromCallable(() -> processUserMessage(userId, userMessage))
                .flatMapMany(result -> {
                    if (result.isStreaming()) {
                        // ✅ Gemini 스트림 전체 Flux
                        Flux<String> stream = geminiApiClient.generateStream(result.prompt())
                                .doOnSubscribe(sub -> log.info("📡 Gemini 스트림 시작 (step={})", result.stepIndex()));

                        // ✅ Flux 버퍼링 후 한 번에 저장
                        return stream
                                .doOnNext(chunk -> log.debug("🧩 Gemini 응답 조각: {}", chunk))
                                .collectList()
                                .flatMapMany(chunks -> {
                                    String merged = String.join("", chunks);
                                    return trySaveTodoAndStepsReactive(userId, merged, result.stepIndex())
                                            .thenMany(Flux.fromIterable(chunks)); // 원본 스트림 그대로 반환
                                })
                                .thenMany(
                                        Mono.defer(() -> {
                                            if (result.stepIndex() == 1) {
                                                // Step1 완료 후 질문 생성
                                                return Mono.fromCallable(() -> {
                                                    UserConversation convo = conversationRepo.findByUserId(userId)
                                                            .orElse(null);
                                                    if (convo == null)
                                                        return "";
                                                    return ChatbotScript.askStartDate(
                                                            convo.getContent() != null ? convo.getContent() : "",
                                                            convo.getTitle() != null ? convo.getTitle() : "");
                                                }).subscribeOn(Schedulers.boundedElastic());
                                            } else if (result.stepIndex() == 2) {
                                                return Mono.fromCallable(() -> {
                                                    UserConversation convo = conversationRepo.findByUserId(userId)
                                                            .orElse(null);
                                                    if (convo == null)
                                                        return "계획 정보를 찾을 수 없습니다 😢";

                                                    StringBuilder sb = new StringBuilder();
                                                    sb.append("우와! 엄청 구체적인데? 지금까지 나온 내용을 정리해볼게.\n\n[")
                                                            .append(convo.getTitle()).append("]\n")
                                                            .append("기간: ").append(convo.getStartDate()).append(" ~ ")
                                                            .append(convo.getEndDate()).append("\n")
                                                            .append("요일: ").append(convo.getStudyDays()).append("\n")
                                                            .append("1회 집중시간: ").append(convo.getDailyMinutes())
                                                            .append("분\n\n이제 마지막 단계야. 이 정보를 바탕으로 너에게 맞는 상세 계획표를 만들어줄게!\n\n");

                                                    // ✅ steps 출력 (pendingPlanJson 기반)
                                                    if (convo.getPendingPlanJson() != null) {
                                                        try {
                                                            TodoStepResponse parsed = objectMapper.readValue(
                                                                    convo.getPendingPlanJson(), TodoStepResponse.class);
                                                            sb.append("🪜 세부 계획:\n");
                                                            for (var step : parsed.steps()) {
                                                                sb.append("• ").append(step.stepDate())
                                                                        .append(" — ").append(step.description())
                                                                        .append("\n");
                                                            }
                                                        } catch (Exception e) {
                                                            log.warn("⚠️ Step JSON 파싱 실패: {}", e.getMessage());
                                                            sb.append("(세부 단계 정보를 불러올 수 없습니다)\n");
                                                        }
                                                    } else {
                                                        sb.append("(세부 단계 정보가 없습니다)\n");
                                                    }

                                                    sb.append("\n어때? 수정할 부분이 있다면 말해줘.");
                                                    return sb.toString();
                                                }).subscribeOn(Schedulers.boundedElastic());
                                            } else {
                                                return Mono.empty();
                                            }
                                        }));
                    } else {
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
     * 단계별 저장 로직 (Flux 버퍼링 기반)
     */
    private Mono<Void> trySaveTodoAndStepsReactive(Long userId, String mergedContent, int stepIndex) {
        return switch (stepIndex) {
            case 1 -> savePlanDescriptionBuffered(userId, mergedContent);
            case 2 -> saveTodoAndStepsBuffered(userId, mergedContent);
            default -> Mono.empty();
        };
    }

    /**
     * Step 1: 계획 설명 (전체 응답 병합 후 1회 저장)
     */
    private Mono<Void> savePlanDescriptionBuffered(Long userId, String description) {
        return Mono.fromRunnable(() -> {
            try {
                conversationRepo.updateContentByUserId(userId, description);
                log.info("📘 [Buffered] 계획 설명  (description={})", description);
                log.info("📘 [Buffered] 계획 설명 저장 완료 (userId={})", userId);
            } catch (Exception e) {
                log.warn("⚠️ Step1 저장 실패: {}", e.getMessage());
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    private Mono<Void> saveTodoAndStepsBuffered(Long userId, String fullContent) {
        if (fullContent == null || !fullContent.contains("{") || !fullContent.contains("steps")) {
            return Mono.empty();
        }

        Matcher matcher = STEPS_JSON_PATTERN.matcher(fullContent);
        if (!matcher.find())
            return Mono.empty();

        String jsonBlock = matcher.group();

        return Mono.fromCallable(() -> {
            try {
                conversationRepo.updatePendingPlanJson(userId, jsonBlock);
                log.info("📝 [Buffered] 계획 JSON 임시 저장 완료 (userId={})", userId);
            } catch (Exception e) {
                log.warn("⚠️ 임시 저장 실패: {}", e.getMessage());
            }
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * 사용자 입력에 따른 상태 전이 및 프롬프트 생성
     */
    @Transactional
    protected MessageResult processUserMessage(Long userId, String userMessage) {
        UserConversation convo = conversationRepo.findByUserId(userId)
                .orElseGet(() -> {
                    UserConversation uc = new UserConversation();
                    uc.setUserId(userId);
                    uc.setState(ConversationState.INTRO);
                    uc.setPlanSaved(false);
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
                    prompt = ChatbotScript.planDetail(convo.getUserAge(), userMessage.trim());
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
                    try {
                        DayConverter.parseDays(userMessage.trim());
                        convo.setStudyDays(userMessage.trim());
                        response = "좋아! 하루 공부 시간을 (분 단위로) 알려줘.";
                        convo.setState(ConversationState.ASK_TIME_PER_DAY);
                    } catch (Exception e) {
                        response = "(예: 월,수,금) 형식으로 작성해줘!";
                    }

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
                    if (userMessage.contains("좋아") || userMessage.contains("응") || userMessage.contains("저장")) {
                        try {
                            if (convo.getPendingPlanJson() != null && !convo.isPlanSaved()) {
                                TodoStepResponse parsed = objectMapper.readValue(convo.getPendingPlanJson(),
                                        TodoStepResponse.class);

                                Todo todo = Todo.builder()
                                        .userId(userId)
                                        .title(convo.getTitle())
                                        .content(convo.getContent())
                                        .startDate(convo.getStartDate())
                                        .endDate(convo.getEndDate())
                                        .progress(0)
                                        .expectedDays(DayConverter.parseDays(convo.getStudyDays()))
                                        .isCompleted(false)
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
                                convo.setPendingPlanJson(null); // ✅ 임시 JSON 제거
                                conversationRepo.save(convo);

                                log.info("💾 CONFIRM_PLAN 단계에서 Todo 및 Steps 최종 저장 완료 (userId={})", userId);
                            }
                        } catch (Exception e) {
                            log.error("❌ CONFIRM_PLAN 단계 저장 중 오류", e);
                        }

                        response = "좋아! 🎉 이 계획으로 진행할게. 화이팅 💪";
                        convo.setState(ConversationState.FINISHED);
                    } else if (userMessage.contains("아니") || userMessage.contains("수정")) {
                        convo.setPendingPlanJson(null); // ❌ 기존 계획 삭제
                        response = "괜찮아 😊 어떤 점을 수정할까? 목표부터 다시 정해보자";
                        convo.setState(ConversationState.ASK_TASK);
                    } else {
                        response = "이 계획으로 진행할까? (좋아 / 응응 / 아니 / 수정 / 저장)으로 답해줘줘";
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

    private record MessageResult(String response, String prompt, boolean isStreaming, int stepIndex) {
    }
}

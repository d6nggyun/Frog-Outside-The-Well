package com._oormthon.seasonthon.domain.ai.service;

import com._oormthon.seasonthon.domain.ai.client.GeminiApiClient;
import com._oormthon.seasonthon.domain.ai.entity.UserConversation;
import com._oormthon.seasonthon.domain.ai.repository.UserConversationRepository;
import com._oormthon.seasonthon.domain.ai.scripts.ChatbotScript;
import com._oormthon.seasonthon.domain.todo.dto.res.TodoStepResponse;
import com._oormthon.seasonthon.domain.todo.enums.Day;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiChatService {

    private final UserConversationRepository conversationRepo;
    private final GeminiApiClient geminiApiClient;
    private final ObjectMapper objectMapper;
    private final ConversationStateService conversationStateService;

    private static final Pattern STEPS_JSON_PATTERN = Pattern.compile("\\{.*\"steps\"\\s*:\\s*\\[.*\\].*\\}",
            Pattern.DOTALL);

    /**
     * 사용자 메시지 처리 (Gemini 스트리밍 포함)
     * ConversationStateService.processUserMessageTransactional 로 분리
     * - 트랜잭션/블로킹 DB 호출은 boundedElastic 스케줄러에서 동작하도록 subscribeOn 적용
     */
    public Flux<String> handleUserMessageStream(Long userId, String userMessageJson) {
        String userMessage = extractMessage(userMessageJson);
        log.info("🗣 사용자 입력: {}", userMessage);

        // processUserMessageTransactional 은 블로킹(동기 JPA)을 사용하므로 boundedElastic 에서 실행
        return Mono.fromCallable(() -> conversationStateService.processUserMessageTransactional(userId, userMessage))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(result -> {
                    if (!result.isStreaming()) {
                        return Flux.just(result.response());
                    }
                    Flux<String> stream = geminiApiClient.generateStream(result.prompt())
                            .doOnSubscribe(sub -> log.info("📡 Gemini 스트림 시작 (step={})", result.stepIndex()));

                    // 여긴 보수적으로 기존 로직을 유지하되, 모든 DB 접근은 boundedElastic로 실행하도록 보장
                    return stream
                            .doOnNext(chunk -> log.debug("🧩 Gemini 응답 조각: {}", chunk))
                            .collectList()
                            .flatMapMany(chunks -> {
                                String merged = String.join("", chunks);
                                // trySaveTodoAndStepsReactive 내부에서 이미 subscribeOn 을 사용하므로 안전
                                return trySaveTodoAndStepsReactive(userId, merged, result.stepIndex())
                                        .thenMany(Flux.fromIterable(chunks));
                            })
                            .thenMany(
                                    Mono.defer(() -> {
                                        if (result.stepIndex() == 1) {
                                            // Step1 완료 후 질문 생성
                                            return Mono.fromCallable(() -> {
                                                Optional<UserConversation> convoOpt = conversationRepo
                                                        .findByUserId(userId);
                                                if (convoOpt.isEmpty())
                                                    return "";
                                                UserConversation convo = convoOpt.get();
                                                return ChatbotScript.askStartDate(
                                                        convo.getContent() != null ? convo.getContent() : "",
                                                        convo.getTitle() != null ? convo.getTitle() : "");
                                            })
                                                    .subscribeOn(Schedulers.boundedElastic());
                                        } else if (result.stepIndex() == 2) {
                                            return Mono.fromCallable(() -> {
                                                Optional<UserConversation> convoOpt = conversationRepo
                                                        .findByUserId(userId);
                                                if (convoOpt.isEmpty())
                                                    return "계획 정보를 찾을 수 없습니다 😢";
                                                UserConversation convo = convoOpt.get();

                                                StringBuilder sb = new StringBuilder(
                                                        ChatbotScript.planSummary(convo));

                                                if (convo.getPendingPlanJson() != null) {
                                                    try {
                                                        TodoStepResponse parsed = objectMapper.readValue(
                                                                convo.getPendingPlanJson(), TodoStepResponse.class);
                                                        sb.append("🪜 세부 계획:\n");
                                                        for (var step : parsed.steps()) {
                                                            sb.append("• ").append(step.stepDate()).append("(")
                                                                    .append(Day.toKorean(step.day())).append(")\n — ")
                                                                    .append(step.description()).append("\n");
                                                            // ✅ Tips 출력 추가
                                                            if (step.tips() != null && !step.tips().isEmpty()) {
                                                                for (String tip : step.tips()) {
                                                                    sb.append("   💡 ").append(tip).append("\n");
                                                                }
                                                            }

                                                            sb.append("\n"); // step 간 간격
                                                        }
                                                    } catch (Exception e) {
                                                        log.warn("⚠️ Step JSON 파싱 실패: {}", e.getMessage());
                                                        sb.append("(세부 단계 정보를 불러올 수 없습니다)\n");
                                                    }
                                                } else {
                                                    sb.append("(세부 단계 정보가 없습니다)\n");
                                                }

                                                sb.append("\n이 계획으로 진행해도 될까?");
                                                return sb.toString();
                                            })
                                                    .subscribeOn(Schedulers.boundedElastic());
                                        } else {
                                            return Mono.empty();
                                        }
                                    }));

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

    private Mono<Void> savePlanDescriptionBuffered(Long userId, String fullContent) {
        return Mono.fromRunnable(() -> {
            try {
                // JSON 파싱
                JsonNode jsonNode = objectMapper.readTree(fullContent);
                String title = jsonNode.path("title").asText(null);
                String content = jsonNode.path("content").asText(null);

                if (title != null) {
                    conversationRepo.updateTitleByUserId(userId, title);
                }
                if (content != null) {
                    conversationRepo.updateContentByUserId(userId, content);
                }

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
            } catch (Exception e) {
                log.warn("⚠️ 임시 저장 실패: {}", e.getMessage());
            }
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

}

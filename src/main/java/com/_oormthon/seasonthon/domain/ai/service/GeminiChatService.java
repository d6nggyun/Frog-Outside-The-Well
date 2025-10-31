package com._oormthon.seasonthon.domain.ai.service;

import com._oormthon.seasonthon.domain.ai.client.GeminiApiClient;
import com._oormthon.seasonthon.domain.ai.entity.UserConversation;
import com._oormthon.seasonthon.domain.ai.enums.ConversationState;
import com._oormthon.seasonthon.domain.ai.repository.UserConversationRepository;
import com._oormthon.seasonthon.domain.ai.scripts.ChatbotScript;
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

@Slf4j
@Service
public class GeminiChatService {

    private final UserConversationRepository conversationRepo;
    private final GeminiApiClient geminiApiClient;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final ObjectMapper objectMapper = new ObjectMapper(); // ✅ JSON 파싱용

    public GeminiChatService(UserConversationRepository repo, GeminiApiClient client) {
        this.conversationRepo = repo;
        this.geminiApiClient = client;
    }

    /**
     * 사용자 메시지를 단계별로 처리하고 필요 시 Gemini SSE 응답 Flux로 반환
     */
    public Flux<String> handleUserMessageStream(Long userId, String userMessageJson) {
        String userMessage = extractMessage(userMessageJson);
        return Mono.defer(() -> Mono.fromCallable(() -> processUserMessage(userId, userMessage)))
                .flatMapMany(result -> {
                    if (result.isStreaming()) {
                        // ✅ SSE 기반 Gemini 스트림 요청 후, 후속 문장 추가
                        return geminiApiClient.generateStream(result.prompt())
                                .concatWith(Flux.interval(Duration.ofSeconds(10)).map(tick -> "💓 연결 유지 중...").take(5)); // 50초간
                                                                                                                         // 유지
                        // .concatWith(Flux.just("\n\n이 계획으로 진행할까?"));
                    } else {
                        // ✅ 일반 텍스트 응답
                        return Flux.just(result.response());
                    }
                })
                .doOnSubscribe(sub -> log.info("💬 [{}] 사용자 입력 처리 시작: {}", userId, userMessage))
                .doOnError(e -> log.error("💥 handleUserMessageStream error: {}", e.getMessage(), e))
                .onErrorResume(e -> Flux.just("죄송해 😢 잠시 문제가 생겼어. 다시 시도해줄래?"));
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
                    convo.setCurrentGoal(userMessage.trim());
                    response = ChatbotScript.askStartDate(convo.getCurrentGoal());
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
                        convo.setDailyMinutes(Integer.parseInt(userMessage.trim()));
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

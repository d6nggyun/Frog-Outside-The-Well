package com._oormthon.seasonthon.domain.ai.service;

// import com._oormthon.seasonthon.domain.ai.repository.UserConversationRepository;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ChatbotScriptService {

    private final GeminiChatService geminiChatService;

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<Long, Disposable> activeStreams = new ConcurrentHashMap<>();

    public ChatbotScriptService(GeminiChatService geminiChatService) {
        this.geminiChatService = geminiChatService;
    }

    /**
     * ✅ SSE 연결 생성 (프론트가 최초 connect 시 호출)
     */
    public SseEmitter connect(Long userId) {
        closeExisting(userId);

        SseEmitter emitter = new SseEmitter(0L); // 무제한 타임아웃
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> {
            log.info("🧵 SSE 연결 종료 userId={}", userId);
            closeExisting(userId);
        });

        emitter.onTimeout(() -> {
            log.warn("⏰ SSE 타임아웃 userId={}", userId);
            closeExisting(userId);
        });

        send(emitter, "안녕! 🐸\n나는 함께 공부계획을 세워주는 개구리 ‘꾸꾸’야!\n" +
                "너가 목표를 세우고 달성할 때마다 나는 우물 밖 세상을 구경할 수 있어.\n" +
                "나랑 함께 점프해볼래? 준비됐어?");
        return emitter;
    }

    /**
     * ✅ 사용자 메시지 처리 (프론트에서 /send 호출 시)
     */
    public void handleUserMessage(Long userId, String userMessage) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) {
            log.warn("🚫 연결된 SSE 없음 userId={}", userId);
            return;
        }

        // 기존 스트림 종료
        closeStream(userId);

        Flux<String> flux = geminiChatService.handleUserMessageStream(userId, userMessage);

        Disposable subscription = flux
                .delayElements(Duration.ofMillis(80))
                .subscribe(
                        data -> send(emitter, data),
                        error -> send(emitter, "❌ 오류: " + error.getMessage()),
                        () -> send(emitter, "✅ 응답 완료"));

        activeStreams.put(userId, subscription);
    }

    private void send(SseEmitter emitter, String data) {
        try {
            emitter.send(SseEmitter.event()
                    .name("message")
                    .data(data));
        } catch (IOException e) {
            log.error("🚨 SSE 전송 오류: {}", e.getMessage());
        }
    }

    private void closeStream(Long userId) {
        if (activeStreams.containsKey(userId)) {
            activeStreams.get(userId).dispose();
            activeStreams.remove(userId);
        }
    }

    private void closeExisting(Long userId) {
        closeStream(userId);
        if (emitters.containsKey(userId)) {
            emitters.get(userId).complete();
            emitters.remove(userId);
        }
    }

    @PreDestroy
    public void cleanup() {
        emitters.values().forEach(SseEmitter::complete);
        activeStreams.values().forEach(Disposable::dispose);
        emitters.clear();
        activeStreams.clear();
        log.info("🧹 ChatbotScriptService 종료 — 모든 연결 해제");
    }

    public void disconnect(Long userId) {
        log.info("🧩 사용자 종료 요청 — userId={}", userId);
        closeExisting(userId);
    }
}

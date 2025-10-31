package com._oormthon.seasonthon.domain.ai.controller;

import com._oormthon.seasonthon.domain.ai.service.GeminiChatService;
import com._oormthon.seasonthon.domain.ai.service.ChatbotScriptService;
import com._oormthon.seasonthon.domain.ai.service.GeminiService;
import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.todo.dto.req.TodoRequest;
import com._oormthon.seasonthon.domain.todo.dto.res.TodoStepResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai")
public class GeminiController implements GeminiApiSpecification {

    private final GeminiService aiService;
    private final ChatbotScriptService chatbotScriptService;

    @Override
    @PostMapping("/{todoId}/generate")
    public ResponseEntity<TodoStepResponse> breakdownTodo(@AuthenticationPrincipal User user,
            @PathVariable Long todoId) {
        TodoStepResponse steps = aiService.breakdownTask(user, todoId);
        return ResponseEntity.status(HttpStatus.CREATED).body(steps);
    }

    /**
     * 🔹 SSE 연결 유지용 (EventSource가 이 엔드포인트에 연결)
     */
    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@RequestParam Long userId) {
        log.info("🌐 SSE 연결 요청 userId={}", userId);
        return chatbotScriptService.connect(userId);
    }

    /**
     * 🔹 사용자 메시지 전송용
     */

    @PostMapping("/send")
    public void sendMessage(@RequestParam Long userId, @RequestBody String message) {
        log.info("📨 사용자 입력 도착 userId={}, message={}", userId, message);
        chatbotScriptService.handleUserMessage(userId, message);
    }

}

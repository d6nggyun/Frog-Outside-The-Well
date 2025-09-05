package com._oormthon.seasonthon.domain.ai.controller;

import com._oormthon.seasonthon.domain.ai.service.GeminiService;
import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.todo.dto.req.TodoRequest;
import com._oormthon.seasonthon.domain.todo.dto.res.TodoStepResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai/test") // 테스트용 경로
public class GeminiTestController {

    private final GeminiService aiService;

    /**
     * 인증 없이 TodoRequest를 받아 Todo를 DB에 저장하고,
     * Gemini로 분해된 TodoStepResponse 리스트를 반환
     */
    @PostMapping
    public ResponseEntity<List<TodoStepResponse>> testBreakdownTodo(
            @Valid @RequestBody TodoRequest request) throws Exception {

        // 테스트용 User 생성 (빌더 방식)
        User testUser = User.builder()
                .userId(1L) // DB 저장용 PK (필요시)
                .email("test@example.com") // 임의 이메일
                .nickname("testUser") // 임의 닉네임
                .kakaoId(12345L) // 임의 카카오ID
                .build();

        // Todo 저장 + Gemini Step 리스트 반환
        List<TodoStepResponse> steps = aiService.breakdownTask(testUser, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(steps);
    }
}

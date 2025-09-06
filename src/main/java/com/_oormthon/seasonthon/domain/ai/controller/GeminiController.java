package com._oormthon.seasonthon.domain.ai.controller;

import com._oormthon.seasonthon.domain.ai.service.GeminiService;
import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.todo.dto.req.TodoRequest;
import com._oormthon.seasonthon.domain.todo.dto.res.TodoStepResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai")
public class GeminiController implements GeminiApiSpecification {

    private final GeminiService aiService;

    @Override
    @PostMapping("/{todoId}/generate")
    public ResponseEntity<TodoStepResponse> breakdownTodo(@AuthenticationPrincipal User user,
            @PathVariable Long todoId) {
        TodoStepResponse steps = aiService.breakdownTask(user, todoId);
        return ResponseEntity.status(HttpStatus.CREATED).body(steps);
    }

}

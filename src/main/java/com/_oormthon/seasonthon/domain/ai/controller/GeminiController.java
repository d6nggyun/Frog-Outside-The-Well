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

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai")
public class GeminiController implements GeminiApiSpecification {

    private final GeminiService aiService;

    @Override
    @PostMapping
    public ResponseEntity<List<TodoStepResponse>> breakdownTodo(@AuthenticationPrincipal User user,
            @Valid @RequestBody TodoRequest request) {
        List<TodoStepResponse> steps = aiService.breakdownTask(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(steps);
    }

}

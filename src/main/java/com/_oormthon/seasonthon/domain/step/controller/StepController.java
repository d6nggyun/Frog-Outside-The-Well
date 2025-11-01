package com._oormthon.seasonthon.domain.step.controller;

import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.step.dto.req.UpdateStepRequest;
import com._oormthon.seasonthon.domain.step.dto.req.UpdateStepRequestId;
import com._oormthon.seasonthon.domain.step.dto.res.OneStepResponse;
import com._oormthon.seasonthon.domain.step.dto.res.StepResponse;
import com._oormthon.seasonthon.domain.step.service.StepService;
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
@RequestMapping("/api/v1/steps")
public class StepController implements StepApiSpecification {

    private final StepService stepService;

    // Step 목록 조회
    @GetMapping("/todos/{todoId}")
    public ResponseEntity<TodoStepResponse> getTodoSteps(@AuthenticationPrincipal User user,
            @PathVariable Long todoId) {
        return ResponseEntity.status(HttpStatus.OK).body(stepService.getTodoSteps(user, todoId));
    }

    // 한 걸음 Step 조회
    @GetMapping("/one-step")
    public ResponseEntity<OneStepResponse> getOneSteps(@AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.OK).body(stepService.getOneSteps(user));
    }

    // 한 걸음 Step 조회
    @GetMapping("/one-step/{todoId}")
    public ResponseEntity<OneStepResponse> getOneStepsWithTodoId(@AuthenticationPrincipal User user,
            @PathVariable Long todoId) {
        return ResponseEntity.status(HttpStatus.OK).body(stepService.getOneStepsWithTodoId(user, todoId));
    }

    // Step 수정
    @PutMapping("/{stepId}")
    public ResponseEntity<List<StepResponse>> updateStep(@AuthenticationPrincipal User user,
            @PathVariable Long stepId,
            @Valid @RequestBody UpdateStepRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(stepService.updateStep(user, stepId, request));
    }

    // Step 전체 수정
    @PutMapping("/todo/{todoId}")
    public ResponseEntity<List<StepResponse>> updateSteps(@AuthenticationPrincipal User user,
            @PathVariable Long todoId,
            @Valid @RequestBody List<UpdateStepRequestId> request) {
        return ResponseEntity.status(HttpStatus.OK).body(stepService.updateSteps(user, todoId, request));
    }

    // Step 삭제
    @DeleteMapping("/{stepId}")
    public ResponseEntity<List<StepResponse>> deleteStep(@AuthenticationPrincipal User user,
            @PathVariable Long stepId) {
        return ResponseEntity.status(HttpStatus.OK).body(stepService.deleteStep(user, stepId));
    }
}

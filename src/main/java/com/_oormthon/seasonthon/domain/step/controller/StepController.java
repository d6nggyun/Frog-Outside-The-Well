package com._oormthon.seasonthon.domain.step.controller;

import com._oormthon.seasonthon.domain.step.dto.req.UpdateStepRequest;
import com._oormthon.seasonthon.domain.step.dto.res.StepRecordResponse;
import com._oormthon.seasonthon.domain.step.dto.res.StepResponse;
import com._oormthon.seasonthon.domain.step.service.StepService;
import com._oormthon.seasonthon.domain.todo.dto.res.TodoStepResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/steps")
public class StepController implements StepApiSpecification{

    private final StepService stepService;

    // Step 목록 조회
    @GetMapping("/todos/{todoId}")
    public ResponseEntity<TodoStepResponse> getTodoSteps(@PathVariable Long todoId) {
        return ResponseEntity.status(HttpStatus.OK).body(stepService.getTodoSteps(todoId));
    }

    // Step 계획 생성
//    @GetMapping
//    public ResponseEntity<Object> generatePlan() {
//        return ResponseEntity.status(HttpStatus.OK).body(taskPlannerService.generatePlan());
//    }

    // Step 기록 시작
    @PostMapping("/{stepId}/start")
    public ResponseEntity<StepRecordResponse> startStep(@PathVariable Long stepId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stepService.startStep(stepId));
    }

    // Step 기록 종료
    @PostMapping("/{stepId}/stop")
    public ResponseEntity<StepRecordResponse> stopStep(@PathVariable Long stepId) {
        return ResponseEntity.status(HttpStatus.OK).body(stepService.stopStep(stepId));
    }

    // Step 수정
    @PutMapping("/{stepId}")
    public ResponseEntity<List<StepResponse>> updateStep(@PathVariable Long stepId,
                                                         @Valid @RequestBody UpdateStepRequest updateStepRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(stepService.updateStep(stepId, updateStepRequest));
    }

    // Step 삭제
    @DeleteMapping("/{stepId}")
    public ResponseEntity<List<StepResponse>> deleteStep(@PathVariable Long stepId) {
        return ResponseEntity.status(HttpStatus.OK).body(stepService.deleteStep(stepId));
    }
}

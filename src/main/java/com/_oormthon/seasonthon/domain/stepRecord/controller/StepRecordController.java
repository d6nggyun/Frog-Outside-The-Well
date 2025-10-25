package com._oormthon.seasonthon.domain.stepRecord.controller;

import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.stepRecord.dto.req.StepStartRequest;
import com._oormthon.seasonthon.domain.stepRecord.dto.req.StepStopRequest;
import com._oormthon.seasonthon.domain.stepRecord.dto.res.StepRecordResponse;
import com._oormthon.seasonthon.domain.stepRecord.service.StepRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/step-records")
public class StepRecordController implements StepRecordApiSpecification{

    private final StepRecordService stepRecordService;

    // Step 기록 시작
    @PostMapping("/{stepId}/start")
    public ResponseEntity<StepRecordResponse> startStep(@AuthenticationPrincipal User user,
                                                        @PathVariable Long stepId,
                                                        @Valid @RequestBody StepStartRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stepRecordService.startStep(user, stepId, request));
    }

    // Step 기록 일시 정지
    @PostMapping("/{stepId}/pause")
    public ResponseEntity<StepRecordResponse> pauseStep(@AuthenticationPrincipal User user,
                                                        @PathVariable Long stepId,
                                                        @Valid @RequestBody StepStopRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(stepRecordService.pauseStep(user, stepId, request));
    }

    // Step 기록 종료
    @PostMapping("/{stepId}/stop")
    public ResponseEntity<StepRecordResponse> stopStep(@AuthenticationPrincipal User user,
                                                       @PathVariable Long stepId,
                                                       @Valid @RequestBody StepStopRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(stepRecordService.stopStep(user, stepId, request));
    }
}

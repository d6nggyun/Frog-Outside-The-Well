package com._oormthon.seasonthon.domain.stepRecord.service;

import com._oormthon.seasonthon.domain.step.domain.TodoDurationGroup;
import com._oormthon.seasonthon.domain.stepRecord.domain.StepRecord;
import com._oormthon.seasonthon.domain.stepRecord.dto.res.StepRecordDetailResponse;
import com._oormthon.seasonthon.domain.stepRecord.repository.StepRecordRepository;
import com._oormthon.seasonthon.global.exception.CustomException;
import com._oormthon.seasonthon.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StepRecordQueryService {

    private final StepRecordRepository stepRecordRepository;

    public StepRecord getStepRecordByUserIdAndStepId(Long userId, Long stepId) {
        return stepRecordRepository.findByUserIdAndStepId(userId, stepId)
                .orElseThrow(() -> {
                    log.warn("[StepRecord 조회 실패] 존재하지 않는 stepId Id: {}", stepId);
                    return new CustomException(ErrorCode.STEP_RECORD_NOT_FOUND);
                });
    }

    public StepRecordDetailResponse getStepRecordResponseByUserIdAndStepId(Long userId, Long stepId) {
        return stepRecordRepository.findStepRecordDetailResponseByUserIdAndStepId(userId, stepId)
                .orElseThrow(() -> {
                    log.warn("[StepRecordResponse 조회 실패] 존재하지 않는 stepId Id: {}", stepId);
                    return new CustomException(ErrorCode.STEP_RECORD_NOT_FOUND);
                });
    }

    public List<TodoDurationGroup> findTodoDurationGroup(Long userId, LocalDate date) {
        return stepRecordRepository.findTodoDurationGroup(userId, date);
    }
}

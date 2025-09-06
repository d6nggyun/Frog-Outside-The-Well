package com._oormthon.seasonthon.domain.step.service;

import com._oormthon.seasonthon.domain.step.domain.StepRecord;
import com._oormthon.seasonthon.domain.step.domain.TodoStep;
import com._oormthon.seasonthon.domain.step.repository.StepRecordRepository;
import com._oormthon.seasonthon.domain.step.repository.TodoStepRepository;
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
public class StepQueryService {

    private final TodoStepRepository todoStepRepository;
    private final StepRecordRepository stepRecordRepository;

    public TodoStep getTodoStepById(Long stepId) {
        return todoStepRepository.findById(stepId)
                .orElseThrow(() -> {
                    log.warn("[Step 조회 실패] 존재하지 않는 stepId Id: {}", stepId);
                    return new CustomException(ErrorCode.STEP_NOT_FOUND);
                });
    }

    public StepRecord getStepRecordByStepId(Long stepId) {
        return stepRecordRepository.findByStepId(stepId)
                .orElseThrow(() -> {
                    log.warn("[StepRecord 조회 실패] 존재하지 않는 stepId Id: {}", stepId);
                    return new CustomException(ErrorCode.STEP_RECORD_NOT_FOUND);
                });
    }

    public List<TodoStep> findAllByUserIdAndStepDate(Long userId, LocalDate now) {
        return todoStepRepository.findAllByUserIdAndStepDate(userId, now);
    }

    public void validateStepOwnership(Long userId, Long stepId) {
        if (!todoStepRepository.existsByIdAndUserId(stepId, userId)) {
            log.warn("[Step 작업 실패] Step Id: {}, User Id: {} - 권한 없음", stepId, userId);
            throw new CustomException(ErrorCode.STEP_ACCESS_DENIED);
        }
    }

    public int countTodoStepsByUserIdAndDate(Long userId, LocalDate date) {
        return todoStepRepository.countByUserIdAndStepDate(userId, date);
    }

    public int countCompletedTodoStepsByUserIdAndDate(Long userId, LocalDate date) {
        return todoStepRepository.countByUserIdAndIsCompletedTrueAndStepDate(userId, date);
    }

    public List<TodoStep> findAllByStepDate(LocalDate localDate) {
        return todoStepRepository.findAllByStepDate(localDate);
    }
}

package com._oormthon.seasonthon.domain.step.service;

import com._oormthon.seasonthon.domain.step.domain.TodoStep;
import com._oormthon.seasonthon.domain.step.dto.res.StepResponse;
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

    public TodoStep getTodoStepById(Long stepId) {
        return todoStepRepository.findById(stepId)
                .orElseThrow(() -> {
                    log.warn("[Step 조회 실패] 존재하지 않는 stepId Id: {}", stepId);
                    return new CustomException(ErrorCode.STEP_NOT_FOUND);
                });
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

    public List<TodoStep> findAllByStepDateAndUserId(LocalDate localDate, Long userId) {
        return todoStepRepository.findAllByStepDateAndUserId(localDate, userId);
    }

    public List<StepResponse> findAllStepsByUserIdAndStepDate(Long userId, LocalDate now) {
        return todoStepRepository.findAllStepResponseByUserIdAndStepDate(userId, now);
    }

    public List<StepResponse> findAllMissedStepsByUserIdAndStepDate(Long userId, LocalDate now) {
        return todoStepRepository.findAllMissedStepResponseByUserIdAndStepDate(userId, now);
    }

    public List<TodoStep> getAllTodoStepByTodoId(Long todoId) {
        return todoStepRepository.findAllByTodoId(todoId);
    }
    public List<TodoStep> getTodoStepsByUserIdAndMonth(Long userId, LocalDate startDate, LocalDate endDate) {
        return todoStepRepository.findAllTodoStepOverlappingPeriod(userId, startDate, endDate);
    }

    public List<StepResponse> findAllStepsByUserIdAndTodoIdAndStepDate(Long userId, Long todoId, LocalDate date) {
        return todoStepRepository.findAllStepResponseByUserIdAndStepDateAndTodoId(userId, todoId, date);
    }

    public List<StepResponse> findAllMissedStepsByUserIdAndTodoIdAndStepDate(Long userId, Long todoId, LocalDate date) {
        return todoStepRepository.findAllMissedStepResponseByUserIdAndStepDateAndTodoId(userId, todoId, date);
    }
}

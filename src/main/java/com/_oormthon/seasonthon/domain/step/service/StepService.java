package com._oormthon.seasonthon.domain.step.service;

import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.step.domain.TodoStep;
import com._oormthon.seasonthon.domain.step.dto.req.UpdateStepRequest;
import com._oormthon.seasonthon.domain.step.dto.req.UpdateStepRequestId;
import com._oormthon.seasonthon.domain.step.dto.res.OneStepResponse;
import com._oormthon.seasonthon.domain.step.dto.res.StepResponse;
import com._oormthon.seasonthon.domain.step.repository.TodoStepRepository;
import com._oormthon.seasonthon.domain.stepCalendar.service.StepCalendarQueryService;
import com._oormthon.seasonthon.domain.stepCalendar.service.StepCalendarService;
import com._oormthon.seasonthon.domain.stepRecord.repository.StepRecordRepository;
import com._oormthon.seasonthon.domain.todo.domain.Todo;
import com._oormthon.seasonthon.domain.todo.dto.res.TodoStepResponse;
import com._oormthon.seasonthon.domain.todo.enums.TodoText;
import com._oormthon.seasonthon.domain.todo.service.TodoQueryService;
import com._oormthon.seasonthon.global.exception.CustomException;
import com._oormthon.seasonthon.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StepService {

    private final TodoStepRepository todoStepRepository;
    private final StepRecordRepository stepRecordRepository;
    private final StepQueryService stepQueryService;
    private final TodoQueryService todoQueryService;
    private final StepCalendarService stepCalendarService;
    private final StepCalendarQueryService stepCalendarQueryService;

    @Transactional(readOnly = true)
    public TodoStepResponse getTodoSteps(User user, Long todoId) {
        todoQueryService.getTodoById(todoId);
        todoQueryService.validateTodoOwnership(user.getUserId(), todoId);

        Todo todo = todoQueryService.getTodoById(todoId);
        List<TodoStep> todoSteps = todoStepRepository.findByTodoId(todoId);
        String progressText = createProgressText(todo.getProgress());

        return TodoStepResponse.of(todo, progressText, todoSteps.stream().map(StepResponse::of).toList());
    }

    @Transactional(readOnly = true)
    public OneStepResponse getOneSteps(User user) {
        List<StepResponse> todayStepResponses = stepQueryService
                .findAllStepsByUserIdAndStepDate(user.getUserId(), LocalDate.now());
        List<StepResponse> missedStepResponses = stepQueryService
                .findAllMissedStepsByUserIdAndStepDate(user.getUserId(), LocalDate.now());

        return OneStepResponse.of(todayStepResponses, missedStepResponses);
    }

    @Transactional
    public List<StepResponse> updateStep(User user, Long stepId, UpdateStepRequest updateStepRequest) {
        stepQueryService.getTodoStepById(stepId);
        stepQueryService.validateStepOwnership(user.getUserId(), stepId);

        TodoStep todoStep = stepQueryService.getTodoStepById(stepId);
        todoStep.updateStep(updateStepRequest);
        Todo todo = todoQueryService.getTodoById(todoStep.getTodoId());

        return newTodoStepResponse(todo);
    }

    @Transactional
    public List<StepResponse> updateSteps(User user, Long todoId, List<UpdateStepRequestId> updateStepRequestIdList) {
        // 1) 입력 검사
        if (updateStepRequestIdList == null || updateStepRequestIdList.isEmpty()) {
            log.warn("updateSteps called with empty request by user {}", user.getUserId());
            throw new CustomException(ErrorCode.STEP_NOT_FOUND);
        }

        // 2) id 목록 수집
        List<Long> ids = updateStepRequestIdList.stream()
                .map(UpdateStepRequestId::stepId)
                .toList();

        // 3) TodoId + UserId 기반으로 Step 조회 → 다른 Todo/사용자의 Step은 애초에 안 불러옴
        List<TodoStep> steps = todoStepRepository.findByTodoId(todoId);

        // 4) 존재 여부 확인
        if (steps.size() != ids.size()) {
            Set<Long> found = steps.stream().map(TodoStep::getId).collect(Collectors.toSet());
            List<Long> missing = ids.stream().filter(id -> !found.contains(id)).toList();
            log.warn("updateSteps - missing step ids {} requested by user {}", missing, user.getUserId());
            throw new CustomException(ErrorCode.STEP_NOT_FOUND);
        }

        // 5) id -> entity 맵 생성
        Map<Long, TodoStep> stepMap = steps.stream()
                .collect(Collectors.toMap(TodoStep::getId, Function.identity()));

        // 6) 순서대로 업데이트
        for (UpdateStepRequestId usri : updateStepRequestIdList) {
            TodoStep step = stepMap.get(usri.stepId());
            step.updateStepwithId(usri); // 내부 validation 필요
        }

        // 7) Dirty Checking으로 flush (명시적으로 saveAll 해도 무방)
        todoStepRepository.saveAll(steps);

        // 8) Todo 조회
        Todo todo = todoQueryService.getTodoById(todoId);

        // 9) 응답 생성
        return newTodoStepResponse(todo);
    }

    @Transactional
    public List<StepResponse> deleteStep(User user, Long stepId) {
        stepQueryService.getTodoStepById(stepId);
        stepQueryService.validateStepOwnership(user.getUserId(), stepId);

        TodoStep todoStep = stepQueryService.getTodoStepById(stepId);
        Todo todo = todoQueryService.getTodoById(todoStep.getTodoId());

        stepCalendarQueryService.deleteByTodoSteps(List.of(todoStep));

        todoStepRepository.deleteById(stepId);

        return newTodoStepResponse(todo);
    }

    private List<StepResponse> newTodoStepResponse(Todo todo) {
        List<TodoStep> todoSteps = todoStepRepository.findByTodoId(todo.getId());

        return todoSteps.stream().map(StepResponse::of).toList();
    }

    private String createProgressText(Integer progress) {
        if (progress == 100) return TodoText.PROGRESS_100.getText();
        else if (progress > 80) return TodoText.PROGRESS_80.getText();
        else if (progress > 50) return TodoText.PROGRESS_50.getText();
        else if (progress > 20) return TodoText.PROGRESS_20.getText();
        else if (progress >= 0) return TodoText.PROGRESS_0.getText();
        else throw new CustomException(ErrorCode.TODO_PROGRESS_NOT_VALID);
    }
}

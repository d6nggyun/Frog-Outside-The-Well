package com._oormthon.seasonthon.domain.todo.service;

import com._oormthon.seasonthon.domain.StepCalendar.service.StepCalendarQueryService;
import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.step.domain.TodoStep;
import com._oormthon.seasonthon.domain.step.dto.req.StepRequest;
import com._oormthon.seasonthon.domain.step.dto.res.StepResponse;
import com._oormthon.seasonthon.domain.step.repository.TodoStepRepository;
import com._oormthon.seasonthon.domain.todo.domain.Todo;
import com._oormthon.seasonthon.domain.todo.dto.req.TodoRequest;
import com._oormthon.seasonthon.domain.todo.dto.req.UpdateTodoRequest;
import com._oormthon.seasonthon.domain.todo.dto.res.TodoResponse;
import com._oormthon.seasonthon.domain.todo.enums.TodoText;
import com._oormthon.seasonthon.domain.todo.repository.TodoRepository;
import com._oormthon.seasonthon.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class TodoService {

    private final TodoRepository todoRepository;
    private final TodoStepRepository todoStepRepository;
    private final TodoQueryService todoQueryService;
    private final StepCalendarQueryService stepCalendarQueryService;

    @Transactional(readOnly = true)
    public PageResponse<TodoResponse> findTodos(User user) {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("endDate").ascending());
        Page<Todo> todos = todoRepository.findByUserId(user.getUserId(), pageable);

        List<String> warmTexts = getWarmText();
        List<String> usedTexts = new ArrayList<>();

        List<TodoResponse> todoResponses = todos.stream()
                .map(todo -> {
                    List<TodoStep> todoSteps = todoStepRepository.findByTodoId(todo.getId());
                    List<StepResponse> stepResponses = todoSteps.stream()
                            .map(todoStep -> StepResponse.from(todo, todoStep))
                            .toList();
                    String warmMessage = createWarmText(usedTexts, warmTexts);

                    return TodoResponse.from(todo, warmMessage, stepResponses);
                }).toList();

        return PageResponse.from(todos.getTotalPages(), todos.getTotalElements(), todoResponses);
    }

    @Transactional
    public TodoResponse addTodo(User user, TodoRequest todoRequest) {
        Todo todo = Todo.createTodo(user, todoRequest);

        todoRepository.save(todo);

        return TodoResponse.from(todo, randomWarmText(getWarmText()), null);
    }

    @Transactional
    public TodoResponse updateTodo(User user, Long todoId, UpdateTodoRequest updateTodoRequest) {
        Todo todo = todoQueryService.getTodoById(todoId);

        todoQueryService.validateTodoOwnership(user.getUserId(), todoId);

        todo.updateTodo(updateTodoRequest);

        todoStepRepository.deleteAll(todoStepRepository.findByTodoId(todoId));

        List<TodoStep> todoStepList = getAndSaveTodoStep(todo.getId(), user.getUserId(), updateTodoRequest.todoSteps());
        List<StepResponse> stepResponses = todoStepList.stream().map(todoStep -> StepResponse.from(todo, todoStep))
                .toList();

        return TodoResponse.from(todo, randomWarmText(getWarmText()), stepResponses);
    }

    @Transactional
    public void deleteTodo(User user, Long todoId) {
        todoQueryService.getTodoById(todoId);
        todoQueryService.validateTodoOwnership(user.getUserId(), todoId);

        todoRepository.deleteById(todoId);

        List<TodoStep> todoStepList = todoStepRepository.findByTodoId(todoId);

        stepCalendarQueryService.deleteByTodoSteps(todoStepList);

        todoStepRepository.deleteAll(todoStepList);
    }

    @Transactional
    public TodoResponse completeTodo(User user, Long todoId) {
        todoQueryService.getTodoById(todoId);
        todoQueryService.validateTodoOwnership(user.getUserId(), todoId);

        Todo todo = todoQueryService.getTodoById(todoId);

        todo.completeTodo();

        List<TodoStep> todoStepList = todoStepRepository.findByTodoId(todoId);
        List<StepResponse> stepResponses = todoStepList.stream().map(todoStep -> StepResponse.from(todo, todoStep))
                .toList();

        return TodoResponse.from(todo, "개구리가 우물 탈출에 성공했어요!", stepResponses);
    }

    private List<TodoStep> getAndSaveTodoStep(Long todoId, Long userId, List<StepRequest> stepList) {
        return todoStepRepository.saveAll(stepList.stream()
                .map(stepRequest -> TodoStep.createTodoStep(todoId, userId, stepRequest)).toList());
    }

    private List<String> getWarmText() {
        return Arrays.stream(TodoText.values())
                .filter(text -> text.name().startsWith("WARM_TEXT"))
                .map(TodoText::getText).toList();
    }

    private String createWarmText(List<String> usedTexts, List<String> allTexts) {
        List<String> availableTexts = new ArrayList<>(allTexts);
        availableTexts.removeAll(usedTexts);

        if (availableTexts.isEmpty()) {
            usedTexts.clear();
            availableTexts = new ArrayList<>(allTexts);
        }

        String selectedText = availableTexts.get(ThreadLocalRandom.current().nextInt(availableTexts.size()));
        usedTexts.add(selectedText);

        return selectedText;
    }

    private String randomWarmText(List<String> warmTexts) {
        return warmTexts.get(ThreadLocalRandom.current().nextInt(warmTexts.size()));
    }
}

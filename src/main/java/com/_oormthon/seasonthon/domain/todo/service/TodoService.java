package com._oormthon.seasonthon.domain.todo.service;

import com._oormthon.seasonthon.domain.stepCalendar.service.StepCalendarQueryService;
import com._oormthon.seasonthon.domain.ai.client.GeminiApiClient;
import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.step.domain.TodoStep;
import com._oormthon.seasonthon.domain.step.repository.TodoStepRepository;
import com._oormthon.seasonthon.domain.stepCalendar.service.StepCalendarService;
import com._oormthon.seasonthon.domain.stepRecord.service.StepRecordQueryService;
import com._oormthon.seasonthon.domain.todo.domain.Todo;
import com._oormthon.seasonthon.domain.todo.dto.req.TodoRequest;
import com._oormthon.seasonthon.domain.todo.dto.req.UpdateTodoDetailRequest;
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
    private final StepRecordQueryService stepRecordQueryService;
    private final StepCalendarService stepCalendarService;
    private final GeminiApiClient geminiApiClient;

    @Transactional(readOnly = true)
    public PageResponse<TodoResponse> findTodos(User user) {
        Pageable pageable = PageRequest.of(0, 20, Sort.by("endDate").ascending());
        Page<Todo> todos = todoRepository.findByUserId(user.getUserId(), pageable);

        List<String> warmTexts = getWarmText();
        List<String> usedTexts = new ArrayList<>();

        List<TodoResponse> todoResponses = todos.stream()
                .map(todo -> {
                    String warmMessage = createWarmText(usedTexts, warmTexts);

                    return TodoResponse.of(todo, warmMessage);
                }).toList();

        return PageResponse.from(todos.getTotalPages(), todos.getTotalElements(), todoResponses);
    }

    @Transactional
    public TodoResponse addTodo(User user, TodoRequest todoRequest) {
        Todo todo = Todo.createTodo(user, todoRequest);

        todoRepository.save(todo);

        return TodoResponse.of(todo, randomWarmText(getWarmText()));
    }

    @Transactional
    public TodoResponse updateTodo(User user, Long todoId, UpdateTodoRequest updateTodoRequest) {
        Todo todo = todoQueryService.getTodoById(todoId);

        todoQueryService.validateTodoOwnership(user.getUserId(), todoId);

        todo.updateTodo(updateTodoRequest);

        todoStepRepository.deleteAll(todoStepRepository.findByTodoId(todoId));

        return TodoResponse.of(todo, randomWarmText(getWarmText()));
    }

    @Transactional
    public TodoResponse updateTodoTitleType(User user, Long todoId, UpdateTodoDetailRequest updateTodoDetailRequest) {
        Todo todo = todoQueryService.getTodoById(todoId);

        todoQueryService.validateTodoOwnership(user.getUserId(), todoId);

        todo.updateTodoDetail(updateTodoDetailRequest);

        return TodoResponse.of(todo, randomWarmText(getWarmText()));
    }

    @Transactional
    public void deleteTodo(User user, Long todoId) {
        todoQueryService.getTodoById(todoId);
        todoQueryService.validateTodoOwnership(user.getUserId(), todoId);

        List<TodoStep> todoStepList = todoStepRepository.findByTodoId(todoId);

        stepCalendarQueryService.deleteByTodoSteps(todoStepList);

        List<Long> stepIds = todoStepList.stream().map(TodoStep::getId).toList();

        stepRecordQueryService.deleteByStepIds(stepIds);

        todoStepList.stream()
                .map(TodoStep::getStepDate)
                .distinct()
                .forEach(date -> stepCalendarService.saveAndUpdateStepCalendar(user.getUserId(), date));

        todoStepRepository.deleteAll(todoStepList);

        todoRepository.deleteById(todoId);
    }

    @Transactional
    public TodoResponse completeTodo(User user, Long todoId) {
        todoQueryService.getTodoById(todoId);
        todoQueryService.validateTodoOwnership(user.getUserId(), todoId);

        Todo todo = todoQueryService.getTodoById(todoId);

        todo.completeTodo();

        return TodoResponse.of(todo, "개구리가 우물 탈출에 성공했어요!");
    }

    private List<String> getWarmText() {
        return Arrays.stream(TodoText.values())
                .filter(text -> text.name().startsWith("WARM_TEXT"))
                .map(TodoText::getText).toList();
    }

    private String createWarmText(List<String> usedTexts, List<String> allTexts) {
        // 1️⃣ AI 프롬프트 작성
        String prompt = """
                당신은 사용자가 하루의 할 일이나 목표를 완료했을 때,
                마음을 따뜻하게 해주는 짧은 격려 문장을 만드는 AI야.

                문장은 너무 현실적이거나 딱딱하지 않고,
                감성적이면서 은유적이면 좋아.
                예를 들어 아래와 같은 문장들을 참고해봐:
                - "매일의 발자국이 모여 큰 걸음이 될거야."
                - "일을 시작하기 전에 심호흡 한 번 크게 해볼까?"
                - "마법의 주문을 외워보자, 나는 할 수 있다."
                - "나... 우물 밖 세상을 구경하고 싶어."

                위 문장들과 비슷한 감정선을 가진 새로운 문장을 하나 만들어줘.
                길이는 한 문장으로, 15자~25자 사이면 좋아.
                반드시 한국어로만, 그리고 인용부호 없이 출력해줘.
                """;

        try {
            String aiMessage = geminiApiClient.generateText(prompt);
            if (aiMessage != null && !aiMessage.isBlank()) {
                return aiMessage.trim();
            }
        } catch (Exception e) {
            log.warn("⚠️ Gemini warm text 생성 실패: {}", e.getMessage());
        }

        // 실패 시 fallback
        String fallback = getRandomWarmTextFallback(usedTexts, allTexts);
        return fallback;
    }

    private String getRandomWarmTextFallback(List<String> usedTexts, List<String> allTexts) {
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

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
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

    private final Map<String, List<String>> warmMsgCache = new ConcurrentHashMap<>();

    @Transactional(readOnly = true)
    public PageResponse<TodoResponse> findTodos(User user) {
        Pageable pageable = PageRequest.of(0, 20, Sort.by("endDate").ascending());

        Slice<Todo> todos = todoRepository.findSliceByUserId(user.getUserId(), pageable);
        List<Todo> todoList = todos.getContent();

        if (todoList.isEmpty()) {
            return PageResponse.from(1, 0, List.of());
        }

        // 캐시 적용
        String cacheKey = buildWarmCacheKey(user.getUserId(), todoList);
        List<String> messages = warmMsgCache.get(cacheKey);

        if (messages == null) {
            messages = generateBulkWarmMessagesForTodos(todoList);
            warmMsgCache.put(cacheKey, messages);
        }

        // 메시지 보정
        List<String> fallback = getWarmText();
        while (messages.size() < todoList.size()) {
            messages.add(randomWarmText(fallback));
        }

        List<TodoResponse> responseList = new ArrayList<>(todoList.size());
        for (int i = 0; i < todoList.size(); i++) {
            responseList.add(TodoResponse.of(todoList.get(i), messages.get(i)));
        }

        return PageResponse.from(
                todos.hasNext() ? 2 : 1,
                todoList.size(),
                responseList);
    }

    private String buildWarmCacheKey(Long userId, List<Todo> todoList) {
        StringBuilder sb = new StringBuilder(userId.toString());
        for (Todo t : todoList)
            sb.append('|').append(t.getTitle());
        return "warm:" + sb.toString().hashCode();
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

    public List<String> generateBulkWarmMessagesForTodos(List<Todo> todoList) {

        StringBuilder todoLines = new StringBuilder();
        for (int i = 0; i < todoList.size(); i++) {
            todoLines.append(String.format("- (%d) %s%n", i + 1, todoList.get(i).getTitle()));
        }

        String prompt = """
                당신은 사용자의 Todo(할 일 목록)에 맞추어 따뜻한 감성 메시지를 생성하는 AI야.
                아래는 사용자의 Todo 목록이야. 각 Todo의 분위기와 상황을 가볍게 참고하여,
                해당 Todo를 할 때 힘이 되는 짧은 응원 문장을 만들어줘.

                [사용자의 Todo 목록]
                %s

                아래 예시 문장들과 유사한 감정선으로 작성하면 좋아:
                - "매일의 발자국이 모여 큰 걸음이 될거야."
                - "일을 시작하기 전에 심호흡 한 번 크게 해볼까?"
                - "마법의 주문을 외워보자, 나는 할 수 있다."
                - "나... 우물 밖 세상을 구경하고 싶어."

                [생성 규칙]
                - Todo 개수는 총 %d개이며, 반드시 Todo 순서와 동일한 순서로 문장을 생성할 것
                - 한국어 15~25자의 한 문장
                - 인용부호(따옴표) 없이 출력
                - 각 문장 간 감정선은 비슷하되 표현은 서로 달라야 함
                - 지나치게 현실적인 조언(X), 은유적이고 따뜻한 감성(O)
                - JSON 배열 형식으로 출력 (예: ["문장1", "문장2", ...])
                - JSON 외 설명, 여분 텍스트 출력 금지

                이제 Todo별로 총 %d개의 문장을 JSON 배열로 출력해줘.
                """.formatted(todoLines, todoList.size(), todoList.size());

        List<String> messages = geminiApiClient.generateText(prompt);
        if (messages == null)
            messages = new ArrayList<>();

        return messages;

    }

    private String randomWarmText(List<String> warmTexts) {
        return warmTexts.get(ThreadLocalRandom.current().nextInt(warmTexts.size()));
    }
}

package com._oormthon.seasonthon.domain.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com._oormthon.seasonthon.domain.todo.domain.Todo;
import com._oormthon.seasonthon.domain.todo.dto.req.TodoRequest;
import com._oormthon.seasonthon.domain.todo.dto.res.TodoStepResponse;
import com._oormthon.seasonthon.domain.todo.repository.TodoRepository;
import com._oormthon.seasonthon.domain.step.domain.TodoStep;
import com._oormthon.seasonthon.domain.step.repository.TodoStepRepository;
import com._oormthon.seasonthon.global.exception.CustomException;
import com._oormthon.seasonthon.global.exception.ErrorCode;
import com._oormthon.seasonthon.domain.ai.dto.GeminiReqDto;
import com._oormthon.seasonthon.domain.ai.dto.GeminiResDto;
import com._oormthon.seasonthon.domain.member.entity.User;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

        private final ObjectMapper objectMapper;
        private final TodoStepRepository todoStepRepository;
        private final TodoRepository todoRepository;
        private final RestTemplate restTemplate;

        @Value("${gemini.api.key}")
        private String apiKey;

        @Value("${gemini.model.name}")
        private String modelName; // ex) models/gemini-2.5-flash

        private String cleanJsonResponse(String response) {
                if (response == null)
                        return "";
                return response
                                .replaceAll("(?s)```json", "") // ```json 제거
                                .replaceAll("(?s)```", "") // ``` 제거
                                .trim();
        }

        @Transactional
        public List<TodoStepResponse> breakdownTask(User user, TodoRequest todoRequest) {
                // Todo 엔티티 생성
                Todo todo = Todo.createTodo(user, todoRequest);

                String prompt = """
                                당신은 일정 관리 보조 AI입니다.
                                주어진 큰 업무를 실천 가능한 작은 Todo 항목들로 나누세요.

                                반드시 아래 JSON 스키마를 따르세요.
                                마크다운 코드블록(````json`) 없이 순수 JSON만 반환하세요.

                                [
                                  {
                                    "dDay": "D-3",
                                    "title": "큰 업무 제목",
                                    "endDate": "2025-09-05",
                                    "progressText": "진행 상황 설명",
                                    "progress": 0,
                                    "steps": [
                                      {
                                        "stepDate": "2025-09-02",
                                        "stepOrder": 1,
                                        "description": "세부 작업 설명",
                                        "count": 0,
                                        "isCompleted": false
                                      }
                                    ]
                                  }
                                ]

                                큰 업무명: %s
                                업무 설명: %s
                                시작일: %s
                                마감일: %s
                                """.formatted(
                                todoRequest.title(),
                                todoRequest.content(),
                                todoRequest.startDate(),
                                todoRequest.endDate());

                GeminiReqDto request = new GeminiReqDto();
                request.createGeminiReqDto(prompt);

                try {
                        String url = String.format(
                                        "https://generativelanguage.googleapis.com/v1beta/%s:generateContent",
                                        modelName);
                        log.info("Gemini API 요청 URL: {}", url);

                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        headers.add("X-goog-api-key", apiKey);

                        HttpEntity<GeminiReqDto> entity = new HttpEntity<>(request, headers);

                        GeminiResDto response = restTemplate.postForObject(url, entity, GeminiResDto.class);

                        if (response == null || response.getCandidates() == null
                                        || response.getCandidates().isEmpty()) {
                                log.error("Gemini API 응답이 비어있습니다. response={}", response);
                                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
                        }

                        String description;
                        try {
                                description = response.getCandidates().get(0).getContent().getParts().get(0).getText();
                        } catch (Exception e) {
                                log.error("Gemini API 응답 구조 파싱 실패. response={}", response, e);
                                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
                        }

                        String cleanedDescription = cleanJsonResponse(description);

                        List<TodoStepResponse> todoStepResponses;
                        try {
                                todoStepResponses = objectMapper.readValue(cleanedDescription,
                                                new TypeReference<List<TodoStepResponse>>() {
                                                });
                        } catch (JsonProcessingException e) {
                                log.error("Gemini 응답을 TodoStepResponse 리스트로 변환 실패. description={}", cleanedDescription,
                                                e);
                                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
                        }
                        todoRepository.save(todo);

                        // ✅ StepResponse -> TodoStep 변환 후 저장
                        List<TodoStep> todoSteps = todoStepResponses.stream()
                                        .flatMap(tsr -> tsr.steps().stream()
                                                        .map(step -> TodoStep.builder()
                                                                        .todoId(todo.getId())
                                                                        .userId(user.getUserId())
                                                                        .stepDate(step.stepDate())
                                                                        .stepOrder(step.stepOrder())
                                                                        .description(step.description())
                                                                        .isCompleted(step.isCompleted())
                                                                        .build()))
                                        .toList();
                        todoStepRepository.saveAll(todoSteps);
                        return todoStepResponses;

                } catch (Exception e) {
                        log.error("GeminiService breakdownTask 내부 오류", e);
                        throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
                }
        }
}

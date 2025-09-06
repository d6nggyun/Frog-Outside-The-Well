package com._oormthon.seasonthon.domain.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com._oormthon.seasonthon.domain.todo.domain.Todo;
import com._oormthon.seasonthon.domain.todo.dto.res.TodoStepResponse;
import com._oormthon.seasonthon.domain.todo.service.TodoQueryService;
import com._oormthon.seasonthon.domain.step.domain.TodoStep;
import com._oormthon.seasonthon.domain.step.dto.res.StepResponse;
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
        private final TodoQueryService todoQueryService;
        private final RestTemplate restTemplate;

        @Value("${gemini.api-key}")
        private String apiKey;

        @Value("${gemini.model-name}")
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
        public TodoStepResponse breakdownTask(User user, Long todoId) {
                todoQueryService.validateTodoOwnership(user.getUserId(), todoId);

                Todo todo = todoQueryService.getTodoById(todoId);
                String prompt = """
                                당신은 일정 관리 보조 AI입니다.
                                주어진 큰 업무를 실천 가능한 작은 Todo 항목들로 나누세요.

                                반드시 아래 JSON 스키마를 따르세요.
                                마크다운 코드블록(````json`) 없이 순수 JSON만 반환하세요.

                                description의 내용은 항상 ~하기나 명사형으로 마무리하세요.

                                시작일과 마감일은 항상 고려해섬
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


                                큰 업무명: %s
                                업무 설명: %s
                                시작일: %s
                                마감일: %s
                                """.formatted(
                                todo.getTitle(),
                                todo.getContent(),
                                todo.getStartDate(),
                                todo.getEndDate());

                GeminiReqDto request = new GeminiReqDto();
                request.createGeminiReqDto(prompt);

                try {
                        String url = String.format(
                                        "https://generativelanguage.googleapis.com/v1beta/%s:generateContent",
                                        modelName);
                        // log.info("Gemini API 요청 URL: {}", url);

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

                        String description = response.getCandidates().get(0).getContent().getParts().get(0).getText();
                        String cleanedDescription = cleanJsonResponse(description);

                        TodoStepResponse todoStepResponse = objectMapper.readValue(cleanedDescription,
                                        TodoStepResponse.class);

                        // ✅ StepResponse -> TodoStep 변환 후 저장
                        List<TodoStep> todoSteps = todoStepResponse.steps().stream()
                                        .map(step -> TodoStep.builder()
                                                        .todoId(todoId)
                                                        .userId(user.getUserId())
                                                        .stepDate(step.stepDate())
                                                        .description(step.description())
                                                        .isCompleted(step.isCompleted())
                                                        .build())
                                        .toList();
                        List<TodoStep> savedSteps = todoStepRepository.saveAll(todoSteps);
                        // 4. 저장된 Step -> StepResponse 다시 생성 (stepId 포함)
                        List<StepResponse> stepResponses = savedSteps.stream()
                                        .map(savedStep -> StepResponse.from(todo, savedStep))
                                        .toList();

                        // 5. 최종 응답 DTO 반환 (todoId, todoTitle 반영됨)
                        return TodoStepResponse.from(todo, todoStepResponse.progressText(), stepResponses);

                } catch (Exception e) {
                        log.error("GeminiService breakdownTask 내부 오류", e);
                        throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
                }
        }

}

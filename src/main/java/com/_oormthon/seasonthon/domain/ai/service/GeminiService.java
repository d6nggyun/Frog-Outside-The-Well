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
import com._oormthon.seasonthon.global.config.RestTemplateConfig;
import com._oormthon.seasonthon.global.exception.CustomException;
import com._oormthon.seasonthon.global.exception.ErrorCode;
import com._oormthon.seasonthon.global.response.PageResponse;
import com._oormthon.seasonthon.domain.ai.dto.GeminiReqDto;
import com._oormthon.seasonthon.domain.ai.dto.GeminiResDto;
import com._oormthon.seasonthon.domain.member.entity.User;
import okhttp3.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

        // private final OkHttpClient client = new OkHttpClient();
        private final ObjectMapper objectMapper = new ObjectMapper();

        private final TodoRepository todoRepository;

        @Autowired
        private final RestTemplate restTemplate;

        @Value("${openai.api-key}")
        private String apiKey;

        private final String GEMINI_URL_TEMPLATE = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
                        + apiKey;
        // "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent";

        @Transactional
        public List<TodoStepResponse> breakdownTask(User user, TodoRequest todoRequest) throws Exception {
                Todo todo = Todo.createTodo(user, todoRequest);
                todoRepository.save(todo);

                String prompt = "당신은 일정 관리 보조 AI입니다. " +
                                "주어진 큰 업무를 실천 가능한 작은 Todo 항목들로 나누세요. " +
                                "각 Todo는 JSON 배열 형식으로 반환해야 합니다. " +
                                "형식은 [{\"description\": \"작업 설명\", \"stepDate\": \"YYYY-MM-DD\", \"stepOrder\": 번호}, ...] 입니다. "
                                +
                                "큰 업무명: " + todoRequest.title() + "\n" +
                                "업무 설명: " + todoRequest.content() + "\n" +
                                "시작일: " + todoRequest.startDate() + "\n" +
                                "마감일: " + todoRequest.endDate() + "\n";

                GeminiReqDto request = new GeminiReqDto();
                request.createGeminiReqDto(prompt);
                try {
                        // String url = String.format(GEMINI_URL_TEMPLATE, apiKey);
                        log.info("Url: {}", GEMINI_URL_TEMPLATE);
                        GeminiResDto response = restTemplate.postForObject(GEMINI_URL_TEMPLATE, request,
                                        GeminiResDto.class);
                        // ✅ API 응답 null 체크
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

                        List<TodoStepResponse> steps;
                        try {
                                steps = objectMapper.readValue(description,
                                                new TypeReference<List<TodoStepResponse>>() {
                                                });
                        } catch (JsonProcessingException e) {
                                log.error("Gemini 응답을 TodoStepResponse 리스트로 변환 실패. description={}", description, e);
                                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
                        }

                        return steps; // 클라이언트에는 Response DTO 반환
                } catch (Exception e) {
                        // ✅ 전체 예외 로깅 강화
                        log.error("GeminiService breakdownTask 내부 오류", e);
                        throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
                }
        }
}

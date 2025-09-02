package com._oormthon.seasonthon.domain.todo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskPlannerService {

    private final RestTemplate restTemplate;

    @Value("${openai.api-key}")
    private String apiKey;

    public List<Task> generatePlan(String startDate, String taskDescription) {
        String prompt = """
            시작일: %s
            업무 내용: %s
            위 내용을 날짜별 세부 일정으로 나누어라.
            반드시 JSON 배열로만 응답하라.
            예시:
            [
              {"date": "2025-01-15", "task": "조사"},
              {"date": "2025-01-16", "task": "분석"}
            ]
            """.formatted(startDate, taskDescription);

        // 요청 body
        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o",
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        // POST 요청
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "https://api.openai.com/v1/chat/completions",
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        String response = responseEntity.getBody();

        // JSON 파싱
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(response);
            String content = root.get("choices").get(0).get("message").get("content").asText();
            return mapper.readValue(content, new TypeReference<List<Task>>() {});
        } catch (Exception e) {
            throw new RuntimeException("응답 파싱 실패", e);
        }
    }
}

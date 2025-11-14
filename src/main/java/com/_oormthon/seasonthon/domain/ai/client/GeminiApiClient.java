package com._oormthon.seasonthon.domain.ai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Slf4j
@Component
public class GeminiApiClient {

    private final WebClient webClient;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${gemini.api-key}")
    private String apiKey;

    public GeminiApiClient(WebClient.Builder builder) {
        ConnectionProvider provider = ConnectionProvider.builder("gemini-conn-pool")
                .maxConnections(20)
                .maxIdleTime(Duration.ofSeconds(20))
                .maxLifeTime(Duration.ofMinutes(2))
                .pendingAcquireTimeout(Duration.ofSeconds(5))
                .build();

        HttpClient httpClient = HttpClient.create(provider)
                .protocol(HttpProtocol.H2)
                .keepAlive(true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(180))
                        .addHandlerLast(new WriteTimeoutHandler(180)));

        this.webClient = builder
                .baseUrl("https://generativelanguage.googleapis.com/v1beta/models")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    private String cleanJsonResponse(String response) {
        if (response == null)
            return "";
        return response.replaceAll("(?s)```json", "")
                .replaceAll("(?s)```", "")
                .trim();
    }

    private String cleanJsonWarmMsgResponse(String text) {

        if (text == null)
            return "[]";

        String cleaned = text.trim();

        // 1) 코드블록 제거
        cleaned = cleaned.replaceAll("```json", "")
                .replaceAll("```", "")
                .trim();

        // 2) 배열을 문자열로 감싼 경우
        // "['a','b']" 또는 "[\"a\", \"b\"]"
        if (cleaned.startsWith("\"[") && cleaned.endsWith("]\"")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }

        // 3) 이스케이프 복구
        cleaned = cleaned.replace("\\\"", "\"");

        // 4) 줄바꿈/공백 정리
        cleaned = cleaned.replace("\n", "")
                .replace("\r", "")
                .trim();

        // 5) JSON 배열이 아닌 경우를 대비
        // 하나의 문자열만 온 경우 → 배열로 감싸기
        if (!cleaned.startsWith("[") || !cleaned.endsWith("]")) {
            cleaned = "[\"" + cleaned + "\"]";
        }

        return cleaned;
    }

    private Map<String, Object> buildRequestBody(String prompt) {
        return Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));
    }

    /**
     * ✅ warm message 생성
     */
    public List<String> generateText(String prompt) {
        // 1차 모델
        String primaryModel = "/gemini-2.5-flash:generateContent";

        List<String> result = callGeminiModel(prompt, primaryModel);

        if (result != null)
            return result;

        log.warn("⚠️ 1차 모델 실패 → gemini-2.0-flash 로 fallback 시도");

        // 2차 모델 fallback
        String fallbackModel = "/gemini-2.0-flash:generateContent";

        return callGeminiModel(prompt, fallbackModel);
    }

    private List<String> callGeminiModel(String prompt, String modelPath) {
        try {
            String response = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path(modelPath)
                            .queryParam("key", apiKey)
                            .build())
                    .bodyValue(buildRequestBody(prompt))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(15))
                    .block();

            if (response == null || response.isBlank()) {
                return null;
            }

            JsonNode node = mapper.readTree(response);
            JsonNode textNode = node.at("/candidates/0/content/parts/0/text");

            if (textNode.isMissingNode()) {
                return null;
            }

            String json = cleanJsonWarmMsgResponse(textNode.asText());
            JsonNode arr = mapper.readTree(json);

            List<String> list = new ArrayList<>();
            if (arr.isArray()) {
                arr.forEach(n -> list.add(n.asText()));
            }

            return list;

        } catch (Exception e) {
            log.error("💥 Gemini 모델 호출 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * ✅ SSE 스트림 생성 (fallback 포함)
     */
    public Flux<String> generateStream(String prompt) {
        log.info("🚀 Gemini SSE 요청 시작: {}", prompt);

        StringBuilder buffer = new StringBuilder();
        AtomicInteger curly = new AtomicInteger(0);
        AtomicInteger square = new AtomicInteger(0);

        // 1️⃣ 내부 함수로 모델 호출 로직을 분리
        Function<String, Flux<String>> callGeminiModel = (model) -> webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/" + model + ":streamGenerateContent")
                        .queryParam("key", apiKey)
                        .build())
                .bodyValue(buildRequestBody(prompt))
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchangeToFlux(resp -> {
                    if (resp.statusCode().is5xxServerError()) {
                        log.warn("⚠️ Gemini 서버 오류 ({}): {}", model, resp.statusCode());
                        return Flux.error(new RuntimeException("Gemini overloaded (503)"));
                    }
                    if (resp.statusCode().is4xxClientError()) {
                        log.warn("⚠️ Gemini 클라이언트 오류 ({}): {}", model, resp.statusCode());
                        return Flux.error(new RuntimeException("Gemini client error (4xx)"));
                    }
                    return resp.bodyToFlux(String.class);
                });

        // 2️⃣ 기본 모델 → 503 발생 시 fallback 모델 호출
        return callGeminiModel.apply("gemini-2.5-flash")
                .onErrorResume(e -> {
                    if (e.getMessage().contains("503") || e.getMessage().contains("overloaded")) {
                        log.warn("🔁 Gemini 2.5-flash 과부하, gemini-2.0-flash로 재시도...");
                        return callGeminiModel.apply("gemini-2.0-flash");
                    }
                    log.error("💥 Gemini SSE 오류 발생: {}", e.getMessage());
                    return Flux.just("⚠️ Gemini 모델 호출 실패, fallback 사용");
                })
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                        .maxBackoff(Duration.ofSeconds(10))
                        .onRetryExhaustedThrow((spec, signal) -> new RuntimeException("❌ Gemini SSE 재시도 실패")))
                .flatMap(line -> {
                    if (line == null || line.isBlank())
                        return Flux.empty();

                    buffer.append(line.trim());
                    for (char c : line.toCharArray()) {
                        if (c == '{')
                            curly.incrementAndGet();
                        else if (c == '}')
                            curly.decrementAndGet();
                        else if (c == '[')
                            square.incrementAndGet();
                        else if (c == ']')
                            square.decrementAndGet();
                    }

                    if (curly.get() > 0 || square.get() > 0)
                        return Flux.empty();

                    String json = buffer.toString();
                    buffer.setLength(0);
                    curly.set(0);
                    square.set(0);

                    try {
                        JsonNode node = mapper.readTree(json);
                        if (node.isArray())
                            return Flux.fromIterable(node).flatMap(this::extractText);
                        else
                            return extractText(node);
                    } catch (Exception e) {
                        log.warn("⚠️ SSE 파싱 실패: {}", json, e);
                        return Flux.empty();
                    }
                })
                .doOnSubscribe(s -> log.info("📡 Gemini SSE 연결됨"))
                .doFinally(signal -> log.info("✅ Gemini SSE 스트림 종료 (signal: {})", signal));
    }

    private Flux<String> extractText(JsonNode node) {
        JsonNode textNode = node.at("/candidates/0/content/parts/0/text");
        if (textNode.isMissingNode())
            return Flux.empty();
        String cleaned = cleanJsonResponse(textNode.asText());
        log.info("🧩 Gemini 응답 텍스트 조각: {}", cleaned);
        return Flux.just(cleaned);
    }
}

package com._oormthon.seasonthon.domain.ai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelOption;
import io.netty.handler.logging.LogLevel;
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

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class GeminiApiClient {

    private final WebClient webClient;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${gemini.api-key}")
    private String apiKey;

    private String cleanJsonResponse(String response) {
        if (response == null)
            return "";
        return response
                .replaceAll("(?s)```json", "") // ```json 제거
                .replaceAll("(?s)```", "") // ``` 제거
                .trim();
    }

    public GeminiApiClient(WebClient.Builder builder) {

        // ✅ 안정적인 HTTP/2 + 타임아웃 설정
        HttpClient httpClient = HttpClient.create()
                .protocol(HttpProtocol.H2)
                .keepAlive(true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofSeconds(120))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(120))
                        .addHandlerLast(new WriteTimeoutHandler(120)))
                .wiretap("reactor.netty.http.client.HttpClient", LogLevel.DEBUG);

        this.webClient = builder
                .baseUrl("https://generativelanguage.googleapis.com/v1beta/models")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Google Gemini API SSE 요청
     * 
     * @param prompt - 사용자 입력 텍스트
     * @return Flux<String> - 스트리밍되는 텍스트 조각
     */
    public Flux<String> generateStream(String prompt) {

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))));

        log.info("🚀 Gemini SSE 요청 시작: {}", prompt);
        StringBuilder buffer = new StringBuilder();
        AtomicInteger curly = new AtomicInteger(0);
        AtomicInteger square = new AtomicInteger(0);

        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/gemini-2.5-flash:streamGenerateContent")
                        .queryParam("key", apiKey)
                        .build())
                .bodyValue(requestBody)
                .accept(MediaType.TEXT_EVENT_STREAM) // SSE 수신
                .retrieve()
                .bodyToFlux(String.class)
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

                    // JSON 구조가 아직 닫히지 않으면 계속 누적
                    if (curly.get() > 0 || square.get() > 0) {
                        return Flux.empty();
                    }
                    // JSON 불완전 → 대기
                    String json = buffer.toString();
                    buffer.setLength(0);
                    curly.set(0);
                    square.set(0);

                    try {
                        JsonNode node = mapper.readTree(json);
                        if (node.isArray()) {
                            return Flux.fromIterable(node)
                                    .flatMap(this::extractText);
                        } else {
                            return extractText(node);
                        }

                    } catch (Exception e) {
                        log.warn("⚠️ SSE 파싱 실패: {}", json, e);
                        return Flux.empty();
                    }
                })
                .doOnSubscribe(s -> log.info("📡 Gemini SSE 연결됨"))
                .doOnError(e -> log.error("🔥 Gemini SSE 오류 발생", e))
                .doOnCancel(() -> log.warn("⚠️ Gemini SSE 스트림이 클라이언트에 의해 취소됨"))
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

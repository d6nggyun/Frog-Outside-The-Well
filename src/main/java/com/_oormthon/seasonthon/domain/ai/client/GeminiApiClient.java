package com._oormthon.seasonthon.domain.ai.client;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.util.SimpleTrustManagerFactory;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.handler.codec.http2.HttpToHttp2ConnectionHandler;
import io.netty.handler.codec.http2.Http2ConnectionHandler;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.DefaultHttp2Connection;
import io.netty.handler.codec.http2.Http2FrameListener;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.core.publisher.Flux;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class GeminiApiClient {

    private final WebClient webClient;

    @Value("${gemini.api-key}")
    private String apiKey;

    public GeminiApiClient(WebClient.Builder builder) {

        // ✅ HttpClient 안정화 설정 (타임아웃 + HTTP/2 유지 + keepAlive)
        HttpClient httpClient = HttpClient.create()
                .protocol(HttpProtocol.H2) // 명시적으로 HTTP/2 유지
                .keepAlive(true) // 연결 유지
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofSeconds(120))
                .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(120))
                        .addHandlerLast(new WriteTimeoutHandler(120)))
                .wiretap("reactor.netty.http.client.HttpClient", LogLevel.INFO); // 디버깅용

        this.webClient = builder
                .baseUrl("https://generativelanguage.googleapis.com/v1beta/models")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                // 👇 SSE 응답을 받을 수 있도록 Accept 지정
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.TEXT_EVENT_STREAM_VALUE)
                .build();
    }

    /**
     * Google Gemini API에 스트림 요청을 보냄 (SSE)
     *
     * @param prompt - 사용자 요청 메시지
     * @return Flux<String> - 스트리밍 텍스트 응답
     */
    public Flux<String> generateStream(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))));

        log.info("🚀 Gemini 요청 시작: {}", prompt);

        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/gemini-2.5-flash:streamGenerateContent")
                        .queryParam("key", apiKey)
                        .build())
                .bodyValue(requestBody)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(String.class)
                .flatMap(line -> {
                    if (line.startsWith("data:")) {
                        String json = line.substring(5).trim();
                        log.debug("🧩 Gemini 응답 조각: {}", json);
                        return Flux.just(json);
                    } else {
                        return Flux.empty();
                    }
                })
                .doOnSubscribe(sub -> log.info("📡 Gemini 스트림 연결됨"))
                // 🔥 스트림 중단 시 graceful fallback
                .onErrorResume(e -> {
                    log.error("🔥 Gemini Stream Error: {}", e.getMessage(), e);
                    return Flux.empty();
                })
                .doOnCancel(() -> log.warn("⚠️ Gemini 스트림이 클라이언트에 의해 취소됨"))
                .doFinally(signal -> log.info("✅ Gemini 스트림 종료 (signal: {})", signal));
    }
}

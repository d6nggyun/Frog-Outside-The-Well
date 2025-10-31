package com._oormthon.seasonthon.global.config;

import com._oormthon.seasonthon.global.config.jwt.JwtExceptionFilter;
import com._oormthon.seasonthon.global.config.jwt.JwtFilter;
import com._oormthon.seasonthon.global.config.oauth.CustomOAuth2UserService;
import com._oormthon.seasonthon.global.config.oauth.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

        private static final String[] SWAGGER_WHITELIST = {
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/swagger-ui/index.html",
                        "/v3/api-docs/**"
        };

        private static final String[] JWT_WHITELIST = {
                        "/api/test/jwt",
                        "/oauth2/**",
                        "/login/oauth2/**"
        };

        private static final String[] AI_WHITELIST = {
                        "/api/v1/ai/connect/**",
                        "/api/v1/ai/send/**",
                        "/api/v1/ai/**"
        };

        private final JwtFilter jwtFilter;
        private final JwtExceptionFilter jwtExceptionFilter;
        private final CustomOAuth2UserService oAuth2UserService;
        private final OAuth2SuccessHandler oAuth2SuccessHandler;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

                // ===== 공통 설정 =====
                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .cors(Customizer.withDefaults())
                                .httpBasic(AbstractHttpConfigurer::disable)
                                .formLogin(AbstractHttpConfigurer::disable)
                                .logout(AbstractHttpConfigurer::disable)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

                // ===== AI API는 완전 자유 접근, OAuth2Login 필터 제외 =====
                http.securityMatcher(AI_WHITELIST)
                                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

                // ===== Swagger / JWT 테스트 / OAuth2 로그인 관련 요청 =====
                http.securityMatcher("/**")
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(SWAGGER_WHITELIST).permitAll()
                                                .requestMatchers(JWT_WHITELIST).permitAll()
                                                .anyRequest().authenticated())
                                .oauth2Login(oauth -> oauth
                                                .userInfoEndpoint(user -> user.userService(oAuth2UserService))
                                                .successHandler(oAuth2SuccessHandler));

                // ===== JWT 필터 추가 =====
                http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
                http.addFilterBefore(jwtExceptionFilter, JwtFilter.class);

                return http.build();
        }
}

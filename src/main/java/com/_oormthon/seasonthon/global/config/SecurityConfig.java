package com._oormthon.seasonthon.global.config;

import com._oormthon.seasonthon.global.config.jwt.JwtExceptionFilter;
import com._oormthon.seasonthon.global.config.jwt.JwtFilter;
import com._oormthon.seasonthon.global.config.oauth.CustomOAuth2UserService;
import com._oormthon.seasonthon.global.config.oauth.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
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
                        "/oauth2/**"
        };

        private final JwtFilter jwtFilter;
        private final JwtExceptionFilter jwtExceptionFilter;
        private final CustomOAuth2UserService oAuth2UserService;
        private final OAuth2SuccessHandler oAuth2SuccessHandler;
        private final CorsConfig corsConfig;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
                return httpSecurity
                                .csrf(AbstractHttpConfigurer::disable)
                                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
                                .httpBasic(AbstractHttpConfigurer::disable)
                                .formLogin(AbstractHttpConfigurer::disable)
                                .logout(AbstractHttpConfigurer::disable)

                                // 세션 대신 JWT 사용
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // 권한 설정
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(SWAGGER_WHITELIST).permitAll()
                                                .requestMatchers(JWT_WHITELIST).permitAll()
                                                .anyRequest().authenticated())

                                // OAuth2 설정
                                .oauth2Login(oauth -> oauth
                                                .redirectionEndpoint(redirect -> redirect.baseUri("/oauth2/callback/*"))
                                                .userInfoEndpoint(user -> user.userService(oAuth2UserService))
                                                .successHandler(oAuth2SuccessHandler))
                                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                                .addFilterBefore(jwtExceptionFilter, JwtFilter.class)
                                .build();
        }
}
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

        private final JwtFilter jwtFilter;
        private final JwtExceptionFilter jwtExceptionFilter;
        private final CustomOAuth2UserService oAuth2UserService;
        private final OAuth2SuccessHandler oAuth2SuccessHandler;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                return http
                                .csrf(AbstractHttpConfigurer::disable)
                                .cors(Customizer.withDefaults())
                                .httpBasic(AbstractHttpConfigurer::disable)
                                .formLogin(AbstractHttpConfigurer::disable)
                                .logout(AbstractHttpConfigurer::disable)
                                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                // Swagger 관련 경로는 인증 없이 접근 허용
                                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                                                // 나머지 요청은 인증 필요
                                                .anyRequest().authenticated())
                                .oauth2Login(oauth -> oauth
                                                .userInfoEndpoint(u -> u.userService(oAuth2UserService))
                                                .successHandler(oAuth2SuccessHandler))
                                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                                .addFilterBefore(jwtExceptionFilter, JwtFilter.class)
                                .build();
        }
}

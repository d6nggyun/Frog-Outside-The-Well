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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

        private static final String[] SWAGGER_WHITELIST = {
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/swagger-ui/index.html",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/api/v1/**",
                        "/login",
                        "/oauth/callback/kakao**"
        };

        private final JwtFilter jwtFilter;
        private final JwtExceptionFilter jwtExceptionFilter;
        private final CustomOAuth2UserService oAuth2UserService;
        private final OAuth2SuccessHandler oAuth2SuccessHandler;
        private final CorsConfig corsConfig; // 👈 CorsConfig 주입받음

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                return http
                                .csrf(AbstractHttpConfigurer::disable)
                                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource())) // 👈 직접
                                                                                                              // 호출
                                .httpBasic(AbstractHttpConfigurer::disable)
                                .formLogin(AbstractHttpConfigurer::disable)
                                .logout(AbstractHttpConfigurer::disable)

                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(SWAGGER_WHITELIST).permitAll()
                                                .anyRequest().authenticated())

                                .oauth2Login(oauth -> oauth
                                                .userInfoEndpoint(user -> user.userService(oAuth2UserService))
                                                .successHandler(oAuth2SuccessHandler))

                                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                                .addFilterBefore(jwtExceptionFilter, JwtFilter.class)

                                .build();
        }
}

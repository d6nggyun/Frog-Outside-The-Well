package com._oormthon.seasonthon.global.config.oauth;

import com._oormthon.seasonthon.global.config.jwt.JwtTokenProvider;
import com._oormthon.seasonthon.entity.User;
import com._oormthon.seasonthon.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
        private final JwtTokenProvider jwtTokenProvider;
        private final UserRepository userRepository;

        @Value("${oauth.client-redirect-uri}")
        private String URI;

        @Override
        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                        Authentication authentication) throws IOException {
                User user = userRepository.findByKakaoId(Long.valueOf(authentication.getName()))
                                .orElseThrow(RuntimeException::new);
                String accessToken = jwtTokenProvider.generateAccessToken(user);

                String redirectUrl = UriComponentsBuilder.fromUriString(URI)
                                .queryParam("accessToken", accessToken)
                                .build().toUriString();

                response.sendRedirect(redirectUrl);
        }
}

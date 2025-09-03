package com._oormthon.seasonthon.global.config.jwt;

import com._oormthon.seasonthon.domain.member.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class JwtTestGenerator {

    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "테스트용 JWT 생성", description = "테스트용 JWT를 생성하여 반환합니다. (Swagger UI 접근용)")
    @GetMapping("/api/test/jwt")
    public String generateTestJwt() {
        // 테스트용 User 객체
        User testUser = User.builder()
                .userId(999L)
                .kakaoId(1234567890L)
                .nickname("TestUser")
                .email("testuser@example.com")
                .build();

        // JWT 생성
        return jwtTokenProvider.generateAccessToken(testUser);
    }
}
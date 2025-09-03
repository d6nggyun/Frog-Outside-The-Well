package com._oormthon.seasonthon.global.config.jwt;

import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.member.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Test", description = "테스트 API")
public class JwtTestGenerator {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Operation(summary = "테스트용 JWT 생성", description = "테스트용 JWT를 생성하여 반환합니다. (Swagger UI 접근용)")
    @GetMapping("/api/test/jwt")
    public String generateTestJwt() {
        Long testKakaoId = 1234567890L;

        User testUser = userRepository.findByKakaoId(testKakaoId)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .kakaoId(testKakaoId)
                            .nickname("TestUser")
                            .email("test@gmail.com")
                            .build();
                    return userRepository.save(newUser);
                });

        return jwtTokenProvider.generateAccessToken(testUser);
    }
}
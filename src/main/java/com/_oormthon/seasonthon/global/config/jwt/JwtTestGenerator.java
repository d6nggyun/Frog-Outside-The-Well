package com._oormthon.seasonthon.global.config.jwt;

import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.member.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class JwtTestGenerator {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Operation(summary = "테스트용 JWT 생성", description = "DB에 존재하는 경우 해당 유저로 JWT 생성, 없으면 생성 후 JWT 반환")
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
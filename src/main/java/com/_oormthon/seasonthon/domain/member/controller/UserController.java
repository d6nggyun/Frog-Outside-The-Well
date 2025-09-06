package com._oormthon.seasonthon.domain.member.controller;

import com._oormthon.seasonthon.global.response.DataResponseDto;
import com._oormthon.seasonthon.global.response.ResponseDto;
import com._oormthon.seasonthon.domain.member.dto.response.UserResponse;
import com._oormthon.seasonthon.domain.member.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public ResponseDto<UserResponse> getUser(@PathVariable Long userId) {
        UserResponse res = userService.getUserById(userId);
        return DataResponseDto.of(res);
    }

    @GetMapping("/kakao/{kakaoId}")
    public ResponseDto<UserResponse> getByKakao(@PathVariable Long kakaoId) {
        UserResponse res = userService.getUserByKakaoId(kakaoId);
        return DataResponseDto.of(res);
    }
}

package com._oormthon.seasonthon.domain.member.controller;

import com._oormthon.seasonthon.domain.member.dto.req.UpdateMypageRequest;
import com._oormthon.seasonthon.domain.member.dto.res.UserResponse;
import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.member.service.UserService;
import com._oormthon.seasonthon.global.response.DataResponseDto;
import com._oormthon.seasonthon.global.response.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController implements UserApiSpecification {

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

    @GetMapping("/my-page")
    public ResponseEntity<UserResponse> getMyPage(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.getMyPage(user.getUserId()));
    }

    @PutMapping("/my-page")
    public ResponseEntity<UserResponse> updateMyPage(@AuthenticationPrincipal User user,
                                                     @RequestBody UpdateMypageRequest request) {
        return ResponseEntity.ok(userService.updateMyPage(user.getUserId(), request));
    }
}

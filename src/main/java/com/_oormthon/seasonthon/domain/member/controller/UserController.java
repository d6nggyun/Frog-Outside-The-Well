package com._oormthon.seasonthon.domain.member.controller;

import com._oormthon.seasonthon.global.response.DataResponseDto;
import com._oormthon.seasonthon.global.response.ResponseDto;
import com._oormthon.seasonthon.domain.member.dto.request.KakaoLoginRequest;
import com._oormthon.seasonthon.domain.member.dto.response.UserResponse;
import com._oormthon.seasonthon.domain.member.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class UserController {

    private final UserService memberService;

    @PostMapping("/kakao-login")
    public ResponseDto<UserResponse> kakaoLogin(@Valid @RequestBody KakaoLoginRequest req) {
        UserResponse res = memberService.kakaoLogin(req);
        return DataResponseDto.of(res);
    }

    @GetMapping("/{memberId}")
    public ResponseDto<UserResponse> getMember(@PathVariable Long memberId) {
        UserResponse res = memberService.getMemberById(memberId);
        return DataResponseDto.of(res);
    }

    @GetMapping("/kakao/{kakaoId}")
    public ResponseDto<UserResponse> getByKakao(@PathVariable Long kakaoId) {
        UserResponse res = memberService.getMemberByKakaoId(kakaoId);
        return DataResponseDto.of(res);
    }
}

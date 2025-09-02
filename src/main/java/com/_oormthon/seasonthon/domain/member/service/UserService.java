package com._oormthon.seasonthon.domain.member.service;

import com._oormthon.seasonthon.domain.member.dto.request.KakaoLoginRequest;
import com._oormthon.seasonthon.domain.member.dto.response.UserResponse;

public interface UserService {
    UserResponse kakaoLogin(KakaoLoginRequest req);

    UserResponse getMemberById(Long memberId);

    UserResponse getMemberByKakaoId(Long kakaoId);
}

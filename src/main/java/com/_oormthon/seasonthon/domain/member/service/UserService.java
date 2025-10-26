package com._oormthon.seasonthon.domain.member.service;

import com._oormthon.seasonthon.domain.member.dto.res.UserResponse;

public interface UserService {

    UserResponse getUserById(Long userId);

    UserResponse getUserByKakaoId(Long kakaoId);

    UserResponse getMyPage(Long userId);

    UserResponse updateMyPage(Long userId, UserResponse request);
}

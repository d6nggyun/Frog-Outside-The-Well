package com._oormthon.seasonthon.domain.member.service;

import com._oormthon.seasonthon.domain.member.dto.response.UserResponse;

public interface UserService {

    UserResponse getUserById(Long userId);

    UserResponse getUserByKakaoId(Long kakaoId);
}

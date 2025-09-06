package com._oormthon.seasonthon.domain.member.service.impl;

import com._oormthon.seasonthon.domain.member.dto.response.UserResponse;
import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.member.repository.UserRepository;
import com._oormthon.seasonthon.domain.member.service.UserService;
import com._oormthon.seasonthon.global.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User m = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + userId));
        return UserResponse.fromEntity(m);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByKakaoId(Long kakaoId) {
        User m = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found by kakaoId: " + kakaoId));
        return UserResponse.fromEntity(m);
    }
}

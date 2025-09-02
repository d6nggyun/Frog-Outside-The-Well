package com._oormthon.seasonthon.domain.member.service.impl;

import com._oormthon.seasonthon.domain.member.dto.request.KakaoLoginRequest;
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
    @Transactional
    public UserResponse kakaoLogin(KakaoLoginRequest req) {
        Long kakaoId = req.getKakaoId();

        User user = userRepository.findByKakaoId(kakaoId)
                .map(existing -> {
                    // 필요한 업데이트 (이메일/닉네임 동기화)
                    existing.setEmail(req.getEmail());
                    existing.setNickname(req.getNickname());
                    return userRepository.save(existing);
                })
                .orElseGet(() -> {
                    // 신규 생성: 이메일 중복체크(옵션)
                    userRepository.findByEmail(req.getEmail()).ifPresent(e -> {
                        // 같은 이메일이 이미 있고 카카오ID가 없는 경우 정책에 따라 처리 가능
                        // 여기서는 중복이 있으면 예외로 처리하지 않고 진행하지만 필요시 예외 던지세요.
                    });

                    User newUser = User.builder()
                            .kakaoId(req.getKakaoId())
                            .email(req.getEmail())
                            .nickname(req.getNickname())
                            .build();
                    return userRepository.save(newUser);
                });

        return UserResponse.fromEntity(user);
    }

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

package com._oormthon.seasonthon.domain.member.repository;

import com._oormthon.seasonthon.domain.member.dto.res.UserResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import com._oormthon.seasonthon.domain.member.entity.User;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByKakaoId(Long kakaoId);

    Optional<User> findByEmail(String email);

    @Query("""
    SELECT new com._oormthon.seasonthon.domain.member.dto.res.UserResponse(
        u.userId,
        u.email,
        u.nickname,
        u.kakaoId,
        u.profileImage,
        u.age,
        u.school,
        u.grade
    )
    FROM User u
    WHERE u.userId = :userId
""")
    Optional<UserResponse> findUserResponseByUserId(Long userId);
}
package com._oormthon.seasonthon.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com._oormthon.seasonthon.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByKakaoId(Long kakaoId);
}
package com._oormthon.seasonthon.domain.ai.repository;

import com._oormthon.seasonthon.domain.ai.entity.UserConversation;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserConversationRepository extends JpaRepository<UserConversation, Long> {
    Optional<UserConversation> findByUserId(Long userId);
}

package com._oormthon.seasonthon.domain.ai.repository;

import com._oormthon.seasonthon.domain.ai.entity.UserConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

public interface UserConversationRepository extends JpaRepository<UserConversation, Long> {
    Optional<UserConversation> findByUserId(Long userId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE UserConversation u SET u.content = :content, u.planSaved = false WHERE u.userId = :userId")
    void updateContentByUserId(@Param("userId") Long userId, @Param("content") String content);

    // ✅ 임시 계획 JSON 저장용
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE UserConversation u SET u.pendingPlanJson = :json, u.planSaved = false WHERE u.userId = :userId")
    void updatePendingPlanJson(Long userId, String json);

    // ✅ 임시 계획 JSON 삭제용 (수정 시 "아니"/"수정" 입력 시)
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE UserConversation u SET u.pendingPlanJson = NULL, u.planSaved = false WHERE u.userId = :userId")
    void clearPendingPlanJson(Long userId);
}

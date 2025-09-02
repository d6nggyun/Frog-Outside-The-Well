package com._oormthon.seasonthon.domain.member.dto.response;

import com._oormthon.seasonthon.domain.member.entity.User;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long memberId;
    private String email;
    private String nickname;
    private Long kakaoId;

    public static UserResponse fromEntity(User m) {
        if (m == null)
            return null;
        return UserResponse.builder()
                .memberId(m.getMemberId())
                .email(m.getEmail())
                .nickname(m.getNickname())
                .kakaoId(m.getKakaoId())
                .build();
    }
}

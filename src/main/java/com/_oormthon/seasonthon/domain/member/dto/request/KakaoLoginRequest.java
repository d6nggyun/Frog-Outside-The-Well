package com._oormthon.seasonthon.domain.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KakaoLoginRequest {
    @NotNull
    private Long kakaoId;

    @NotNull
    @Email
    private String email;

    private String nickname;
}

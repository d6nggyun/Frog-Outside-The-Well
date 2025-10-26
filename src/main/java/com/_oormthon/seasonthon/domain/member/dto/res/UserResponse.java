package com._oormthon.seasonthon.domain.member.dto.res;

import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.member.enums.School;

public record UserResponse(

        Long userId,

        String email,

        String nickname,

        Long kakaoId,

        String profileImage,

        Integer age,

        School school,

        Integer grade

) {
    public static UserResponse of(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                user.getKakaoId(),
                user.getProfileImage(),
                user.getAge(),
                user.getSchool(),
                user.getGrade()
        );
    }
}

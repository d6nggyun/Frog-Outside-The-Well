package com._oormthon.seasonthon.domain.member.dto.req;

import com._oormthon.seasonthon.domain.member.enums.School;

public record UpdateMypageRequest(

        Integer age,

        School school,

        Integer grade

) {
}

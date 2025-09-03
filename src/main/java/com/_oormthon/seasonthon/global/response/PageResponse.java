package com._oormthon.seasonthon.global.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "페이지 정보 응답")
public record PageResponse<T>(

        @Schema(description = "총 페이지 수")
        Long pages,

        @Schema(description = "총 게시글 수")
        Long total,

        @Schema(description = "게시글 리스트")
        List<T> contents

) {
    public static <T> PageResponse<T> from(long pages, long total, List<T> contents) {
        return new PageResponse<>(pages, total, contents);
    }
}
package com._oormthon.seasonthon.domain.s3.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PresignedUrlResponse {
        private String uploadUrl;
        private String key;
}

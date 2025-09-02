package com._oormthon.seasonthon.global.response;

public class DataResponseDto {
    public static <T> ResponseDto<T> of(T data) {
        return new ResponseDto<>("S000", "OK", data);
    }

    public static <T> ResponseDto<T> of(String code, String message, T data) {
        return new ResponseDto<>(code, message, data);
    }
}

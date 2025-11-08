package com._oormthon.seasonthon.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Global
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 500, "서버 오류가 발생했습니다."),
    MISSING_PART(HttpStatus.BAD_REQUEST, 400, "요청에 필요한 부분이 없습니다."),
    NO_HANDLER_FOUND(HttpStatus.NOT_FOUND, 404, "요청하신 API가 존재하지 않습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, 405, "지원하지 않는 HTTP 메서드입니다."),

    // Jwt
    JWT_ENTRY_POINT(HttpStatus.UNAUTHORIZED, 401, "인증되지 않은 사용자입니다."),
    JWT_ACCESS_DENIED(HttpStatus.FORBIDDEN, 403, "리소스에 접근할 권한이 없습니다."),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "회원을 찾을 수 없습니다."),

    // ToDo
    TODO_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "ToDo를 찾을 수 없습니다."),
    TODO_ACCESS_DENIED(HttpStatus.FORBIDDEN, 403, "ToDo에 접근할 권한이 없습니다."),
    TODO_NOT_SAME(HttpStatus.NOT_ACCEPTABLE, 401, "같은 ToDo가 아닙니다."),
    TODO_PROGRESS_NOT_VALID(HttpStatus.BAD_REQUEST, 400, "유효하지 않은 ToDo 진행률 값입니다."),

    // Step
    STEP_ACCESS_DENIED(HttpStatus.FORBIDDEN, 403, "Step에 접근할 권한이 없습니다."),
    STEP_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "Step을 찾을 수 없습니다."),
    STEP_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "Step 기록을 찾을 수 없습니다."),
    STEP_NOT_STARTED(HttpStatus.BAD_REQUEST, 400, "시작되지 않은 Step입니다."),
    STEP_INVALID_DURATION(HttpStatus.BAD_REQUEST, 400, "유효하지 않은 duration 값입니다."),

    // Daily Log
    DAILY_LOG_AFTER_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "Daily Log After을 찾을 수 없습니다."),
    DAILY_LOG_BEFORE_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "Daily Log Before을 찾을 수 없습니다."),

    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, 409, "이미 존재하는 리소스입니다."),

    // Validation
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, 400, "요청한 값이 올바르지 않습니다.");

    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;
}

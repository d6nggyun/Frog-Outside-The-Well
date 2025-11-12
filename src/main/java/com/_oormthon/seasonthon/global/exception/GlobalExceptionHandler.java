package com._oormthon.seasonthon.global.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponseEntity> handleCustomException(CustomException ex) {
        return ErrorResponseEntity.toResponseEntity(ex.getErrorCode());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseEntity> handleAuthenticationException(AuthenticationException ex) {
        ErrorCode errorCode = ErrorCode.JWT_ENTRY_POINT;
        return ErrorResponseEntity.toResponseEntity(errorCode);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseEntity> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {
        ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return ErrorResponseEntity.toResponseEntity(errorCode, "요청한 값이 올바르지 않습니다.", errors);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponseEntity> handleMissingPart(MissingServletRequestPartException ex) {
        ErrorCode errorCode = ErrorCode.MISSING_PART;

        String message = "요청에 필요한 부분이 없습니다: " + ex.getRequestPartName();

        return ErrorResponseEntity.toResponseEntity(errorCode, message, null);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponseEntity> handleNoHandlerFound(NoHandlerFoundException ex) {
        ErrorCode errorCode = ErrorCode.NO_HANDLER_FOUND;
        String message = "존재하지 않는 API입니다.";
        return ErrorResponseEntity.toResponseEntity(errorCode, message, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseEntity> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        ErrorCode errorCode = ErrorCode.METHOD_NOT_ALLOWED;
        String message = "지원하지 않는 HTTP 메서드입니다: " + ex.getMethod();
        return ErrorResponseEntity.toResponseEntity(errorCode, message, null);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponseEntity> handleIllegalStateException(IllegalStateException ex) {
        ErrorCode errorCode = ErrorCode.DUPLICATE_RESOURCE; // 새 코드가 없다면 아래 설명 참고
        String message = ex.getMessage() != null ? ex.getMessage() : "이미 존재하는 데이터입니다.";
        return ErrorResponseEntity.toResponseEntity(errorCode, message, null);
    }

    @ExceptionHandler(S3ImageException.class)
    public ResponseEntity<ErrorResponseEntity> handleS3ImageException(S3ImageException ex) {
        String message = ex.getMessage();

        // 메시지 내용에 따라 업로드/다운로드 구분
        ErrorCode errorCode;
        if (message != null && message.contains("다운로드")) {
            errorCode = ErrorCode.S3_DOWNLOAD_FAILED;
        } else {
            errorCode = ErrorCode.S3_UPLOAD_FAILED;
        }

        String finalMessage = message != null ? message : errorCode.getMessage();
        return ErrorResponseEntity.toResponseEntity(errorCode, finalMessage, null);
    }
}
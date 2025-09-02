package com._oormthon.seasonthon.global.config.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtExceptionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Swagger UI와 API docs는 예외 필터 건너뛰기
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            filterChain.doFilter(request, response);
        } catch (MalformedJwtException malformedJwtException) {
            setResponseStatus(response, HttpStatus.FORBIDDEN.value(), "토큰이 형식에 맞지 않습니다.");
        } catch (ExpiredJwtException expiredJwtException) {
            setResponseStatus(response, HttpStatus.FORBIDDEN.value(), "만료된 토큰입니다.");
        }
    }

    private void setResponseStatus(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/text");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(message);
    }
}

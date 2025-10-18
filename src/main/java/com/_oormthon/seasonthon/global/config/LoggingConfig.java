package com._oormthon.seasonthon.global.config;

import com._oormthon.seasonthon.domain.member.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
public class LoggingConfig {

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void loggingPointcut() {}

    @Around("loggingPointcut()")
    public Object logApiRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        String userId = getCurrentUserId(request);
        String method = request.getMethod();
        String uri = request.getRequestURI();
        long start = System.currentTimeMillis();

        try {
            return joinPoint.proceed();
        } finally {
            long duration = System.currentTimeMillis() - start;
            log.info("user={} {} {} {}ms", userId, method, uri, duration);
        }
    }

    private String getCurrentUserId(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() instanceof String) {
            return request.getHeader("X-Forwarded-For");
        } else {
            if(authentication.getPrincipal() instanceof User user) {
                return String.valueOf(user.getUserId());
            }
        }
        return "anonymous";
    }
}
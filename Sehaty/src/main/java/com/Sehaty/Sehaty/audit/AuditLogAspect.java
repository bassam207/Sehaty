package com.Sehaty.Sehaty.audit;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class AuditLogAspect {

    @Before("@annotation(auditLog)")
    public void logAction(JoinPoint joinPoint, AuditLog auditLog) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String user = "anonymous";
        if (authentication != null) {
            user = authentication.getName();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Object[] args = joinPoint.getArgs();

        log.info("AUDIT - User: {}, Action: {}, Details: {}", user, auditLog.action(), Arrays.toString(args));
    }
}

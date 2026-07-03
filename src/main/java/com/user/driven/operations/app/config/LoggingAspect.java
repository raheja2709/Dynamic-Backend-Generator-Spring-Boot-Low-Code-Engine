package com.user.driven.operations.app.config;

import com.user.driven.operations.app.core.model.AuditLog;
import com.user.driven.operations.app.core.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

/**
 * Unified AOP aspect for all logging: method tracing + HTTP audit.
 * Replaces the old AuditInterceptor — one class handles everything.
 *
 * - Controller methods: logs HTTP details (IP, user-agent, endpoint, status, duration) to audit_logs table
 * - Service methods: logs entry/exit with timing
 * - Generator methods: logs entry/exit with timing
 * - Slow methods (>1000ms): logged at WARN level
 * - Exceptions: logged at ERROR level with method context
 *
 * @author Jatin Raheja
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {

    private final AuditLogRepository auditLogRepository;

    public LoggingAspect(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Pointcut("execution(* com.user.driven.operations.app.core.service.impl..*(..))")
    public void serviceLayer() {}

    @Pointcut("execution(* com.user.driven.operations.app.api.controller..*(..))")
    public void controllerLayer() {}

    @Pointcut("execution(* com.user.driven.operations.generator..*(..))")
    public void generatorLayer() {}

    /**
     * Around advice for controller methods — persists HTTP audit log to database.
     */
    @Around("controllerLayer()")
    public Object logControllerExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        HttpServletRequest request = getCurrentRequest();
        long startTime = System.currentTimeMillis();

        log.debug("ENTER {}.{}({})", className, methodName, summarizeArgs(joinPoint.getArgs()));

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            log.debug("EXIT  {}.{}() [{}ms]", className, methodName, duration);

            if (duration > 1000) {
                log.warn("SLOW  {}.{}() took {}ms", className, methodName, duration);
            }

            // Persist audit log for HTTP requests
            persistAuditLog(request, duration, null);

            return result;

        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("FAIL  {}.{}() after {}ms: {}", className, methodName, duration, ex.getMessage());

            // Persist audit log with error
            persistAuditLog(request, duration, ex.getMessage());

            throw ex;
        }
    }

    /**
     * Around advice for service and generator methods — method tracing only (no DB persist).
     */
    @Around("serviceLayer() || generatorLayer()")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String args = summarizeArgs(joinPoint.getArgs());

        log.debug("ENTER {}.{}({})", className, methodName, args);

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            log.debug("EXIT  {}.{}() -> {} [{}ms]", className, methodName, summarizeResult(result), duration);

            if (duration > 1000) {
                log.warn("SLOW  {}.{}() took {}ms", className, methodName, duration);
            }

            return result;

        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("FAIL  {}.{}() after {}ms: {}", className, methodName, duration, ex.getMessage());
            throw ex;
        }
    }

    /**
     * Persists an audit log entry to the database with HTTP context.
     */
    private void persistAuditLog(HttpServletRequest request, long durationMs, String errorMessage) {
        if (request == null) return;

        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setRequestId(getRequestId());
            auditLog.setMethod(request.getMethod());
            auditLog.setEndpoint(truncate(buildEndpoint(request), 500));
            auditLog.setResponseStatus(errorMessage != null ? 500 : 200);
            auditLog.setDurationMs(durationMs);
            auditLog.setIpAddress(getClientIp(request));
            auditLog.setUserAgent(truncate(request.getHeader("User-Agent"), 500));
            auditLog.setErrorMessage(truncate(errorMessage, 2000));

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            // Never let audit logging failure interrupt the request
            log.warn("Failed to persist audit log: {}", e.getMessage());
        }
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attrs != null ? attrs.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String getRequestId() {
        String requestId = MDC.get(RequestIdFilter.MDC_REQUEST_ID_KEY);
        return requestId != null ? requestId : "unknown";
    }

    private String buildEndpoint(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        return query != null ? uri + "?" + query : uri;
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String summarizeArgs(Object[] args) {
        if (args == null || args.length == 0) return "";
        return Arrays.stream(args)
                .map(this::summarizeObject)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }

    private String summarizeResult(Object result) {
        if (result == null) return "void";
        return summarizeObject(result);
    }

    private String summarizeObject(Object obj) {
        if (obj == null) return "null";
        String str = obj.toString();
        return str.length() > 100 ? str.substring(0, 100) + "..." : str;
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return null;
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }
}

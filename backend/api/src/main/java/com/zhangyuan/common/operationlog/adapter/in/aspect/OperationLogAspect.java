package com.zhangyuan.common.operationlog.adapter.in.aspect;

import com.zhangyuan.common.operationlog.annotation.OperationLog;
import com.zhangyuan.common.operationlog.application.OperationLogService;
import com.zhangyuan.common.security.AuthUser;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
@Component
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

    private final OperationLogService operationLogService;
    private final SpelExpressionParser spelParser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public OperationLogAspect(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @Around("@annotation(operationLogAnnotation)")
    public Object logOperation(ProceedingJoinPoint joinPoint, OperationLog operationLogAnnotation) throws Throwable {
        // 解析 SpEL 表达式，获取 resourceId 和 detail
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Object[] args = joinPoint.getArgs();
        String resourceId = evaluateSpel(operationLogAnnotation.resourceId(), method, args);
        String detail = evaluateSpel(operationLogAnnotation.detail(), method, args);

        // 获取当前操作人
        AuthUser currentUser = getCurrentUser();
        Long operatorId = currentUser != null ? currentUser.getId() : null;
        String operatorName = currentUser != null ? currentUser.getNickname() : null;

        // 获取请求 IP
        String ipAddress = getClientIp();

        // 先记录日志（初始状态为成功）
        com.zhangyuan.common.operationlog.domain.model.OperationLog logEntry =
                operationLogService.record(operatorId, operatorName,
                        operationLogAnnotation.type(), operationLogAnnotation.resource(),
                        resourceId, detail, ipAddress);

        try {
            Object result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            // 更新日志为失败
            operationLogService.markFailed(logEntry.getId(), e.getMessage());
            log.warn("Operation failed: type={}, resource={}, id={}, error={}",
                    operationLogAnnotation.type(), operationLogAnnotation.resource(),
                    resourceId, e.getMessage());
            throw e;
        }
    }

    private String evaluateSpel(String expression, Method method, Object[] args) {
        if (expression == null || expression.isBlank()) {
            return null;
        }
        String[] paramNames = parameterNameDiscoverer.getParameterNames(method);
        if (paramNames == null) {
            return expression;
        }
        StandardEvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }
        try {
            Expression expr = spelParser.parseExpression(expression);
            Object value = expr.getValue(context);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            log.warn("Failed to evaluate SpEL expression '{}': {}", expression, e.getMessage());
            return null;
        }
    }

    private AuthUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthUser user) {
            return user;
        }
        return null;
    }

    private String getClientIp() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        HttpServletRequest request = attrs.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}

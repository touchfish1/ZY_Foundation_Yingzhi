package com.zhangyuan.user.application.service;

import com.zhangyuan.user.domain.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class AuditLogServiceTest {

    private final AuditLogRepository auditLogRepo = mock(AuditLogRepository.class);
    private AuditLogService service;

    @BeforeEach
    void setUp() {
        service = new AuditLogService(auditLogRepo);
    }

    @Test
    void log_createsEntry() {
        service.log(1L, "LOGIN", "用户登录");
        verify(auditLogRepo).save(any());
    }

    @Test
    void log_withResource() {
        service.log(1L, "ORDER_CREATED", "order", "ORD001", "创建订单", "127.0.0.1", "Mozilla");
        verify(auditLogRepo).save(any());
    }
}

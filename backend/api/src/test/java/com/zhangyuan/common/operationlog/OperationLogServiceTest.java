package com.zhangyuan.common.operationlog;

import com.zhangyuan.common.operationlog.application.OperationLogService;
import com.zhangyuan.common.operationlog.domain.model.OperationLog;
import com.zhangyuan.common.operationlog.domain.model.OperationType;
import com.zhangyuan.common.operationlog.domain.model.ResourceType;
import com.zhangyuan.common.operationlog.domain.repository.OperationLogRepository;
import com.zhangyuan.common.operationlog.dto.OperationLogResponse;
import com.zhangyuan.common.response.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperationLogServiceTest {

    @Mock
    private OperationLogRepository operationLogRepository;

    private OperationLogService operationLogService;

    @BeforeEach
    void setUp() {
        operationLogService = new OperationLogService(operationLogRepository);
    }

    @Test
    void record() {
        OperationLog savedLog = new OperationLog(1L, "Admin", OperationType.CREATE,
                ResourceType.CMS_PAGE, null, null, "127.0.0.1");
        savedLog.setId(1L);
        when(operationLogRepository.save(any(OperationLog.class))).thenReturn(savedLog);

        OperationLog result = operationLogService.record(1L, "Admin", OperationType.CREATE,
                ResourceType.CMS_PAGE, null, null, "127.0.0.1");

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.isSuccess()).isTrue();
        verify(operationLogRepository).save(any(OperationLog.class));
    }

    @Test
    void markFailed() {
        OperationLog existingLog = new OperationLog(1L, "Admin", OperationType.UPDATE,
                ResourceType.CMS_PAGE, "10", null, "127.0.0.1");
        existingLog.setId(1L);
        when(operationLogRepository.findById(1L)).thenReturn(Optional.of(existingLog));
        when(operationLogRepository.save(any(OperationLog.class))).thenReturn(existingLog);

        operationLogService.markFailed(1L, "Something went wrong");

        assertThat(existingLog.isSuccess()).isFalse();
        assertThat(existingLog.getErrorMessage()).isEqualTo("Something went wrong");
        verify(operationLogRepository).save(existingLog);
    }

    @Test
    void markFailedNotFound() {
        when(operationLogRepository.findById(99L)).thenReturn(Optional.empty());

        operationLogService.markFailed(99L, "error");

        // Should not save when the log entry doesn't exist
        verify(operationLogRepository).findById(99L);
        verify(operationLogRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void query() {
        OperationLog log1 = new OperationLog(1L, "Admin", OperationType.QUERY,
                ResourceType.CMS_PAGE, null, null, "127.0.0.1");
        log1.setId(1L);
        OperationLog log2 = new OperationLog(2L, "Admin", OperationType.CREATE,
                ResourceType.ORDER, null, null, "10.0.0.1");
        log2.setId(2L);

        when(operationLogRepository.query(null, null, null, null, null, 1, 20))
                .thenReturn(List.of(log1, log2));
        when(operationLogRepository.count(null, null, null, null, null))
                .thenReturn(2L);

        PageResponse<OperationLogResponse> result = operationLogService.query(
                null, null, null, null, null, 1, 20);

        assertThat(result.items()).hasSize(2);
        assertThat(result.total()).isEqualTo(2);
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.items().getFirst().operationType()).isEqualTo("QUERY");
        assertThat(result.items().get(1).resourceType()).isEqualTo("ORDER");
    }

    @Test
    void queryWithFilters() {
        Instant start = Instant.parse("2026-01-01T00:00:00Z");
        Instant end = Instant.parse("2026-12-31T23:59:59Z");

        when(operationLogRepository.query(eq(1L), eq(ResourceType.CMS_PAGE), eq(OperationType.CREATE),
                eq(start), eq(end), eq(1), eq(10)))
                .thenReturn(List.of());
        when(operationLogRepository.count(eq(1L), eq(ResourceType.CMS_PAGE), eq(OperationType.CREATE),
                eq(start), eq(end)))
                .thenReturn(0L);

        PageResponse<OperationLogResponse> result = operationLogService.query(
                1L, ResourceType.CMS_PAGE, OperationType.CREATE, start, end, 1, 10);

        assertThat(result.items()).isEmpty();
        assertThat(result.total()).isZero();
    }
}

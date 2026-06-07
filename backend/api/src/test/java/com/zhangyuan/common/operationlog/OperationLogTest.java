package com.zhangyuan.common.operationlog;

import com.zhangyuan.common.operationlog.domain.model.OperationLog;
import com.zhangyuan.common.operationlog.domain.model.OperationType;
import com.zhangyuan.common.operationlog.domain.model.ResourceType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OperationLogTest {

    @Test
    void createSuccessLog() {
        OperationLog log = new OperationLog(1L, "Admin", OperationType.CREATE,
                ResourceType.CMS_PAGE, null, null, "127.0.0.1");

        assertThat(log.getOperatorId()).isEqualTo(1L);
        assertThat(log.getOperatorName()).isEqualTo("Admin");
        assertThat(log.getOperationType()).isEqualTo(OperationType.CREATE);
        assertThat(log.getResourceType()).isEqualTo(ResourceType.CMS_PAGE);
        assertThat(log.getResourceId()).isNull();
        assertThat(log.isSuccess()).isTrue();
        assertThat(log.getErrorMessage()).isNull();
        assertThat(log.getIpAddress()).isEqualTo("127.0.0.1");
        assertThat(log.getCreatedAt()).isNotNull();
    }

    @Test
    void createLogWithResourceId() {
        OperationLog log = new OperationLog(1L, "Admin", OperationType.UPDATE,
                ResourceType.CMS_PAGE, "42", "{\"slug\": \"/new\"}", "10.0.0.1");

        assertThat(log.getResourceId()).isEqualTo("42");
        assertThat(log.getDetail()).isEqualTo("{\"slug\": \"/new\"}");
    }

    @Test
    void markFailed() {
        OperationLog log = new OperationLog(1L, "Admin", OperationType.DELETE,
                ResourceType.CMS_PAGE, "5", null, "127.0.0.1");

        assertThat(log.isSuccess()).isTrue();

        log.markFailed("Page not found");
        assertThat(log.isSuccess()).isFalse();
        assertThat(log.getErrorMessage()).isEqualTo("Page not found");
    }

    @Test
    void differentOperationTypes() {
        assertThat(OperationType.CREATE).isNotNull();
        assertThat(OperationType.UPDATE).isNotNull();
        assertThat(OperationType.DELETE).isNotNull();
        assertThat(OperationType.QUERY).isNotNull();
        assertThat(OperationType.OTHER).isNotNull();
    }

    @Test
    void differentResourceTypes() {
        assertThat(ResourceType.CMS_PAGE).isNotNull();
        assertThat(ResourceType.ORDER).isNotNull();
        assertThat(ResourceType.PRODUCT_PLAN_GROUP).isNotNull();
        assertThat(ResourceType.ASSET_FILE).isNotNull();
    }
}

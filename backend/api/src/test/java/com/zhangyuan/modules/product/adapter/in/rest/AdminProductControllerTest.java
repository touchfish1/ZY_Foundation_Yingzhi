package com.zhangyuan.modules.product.adapter.in.rest;

import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.product.application.service.ProductApplicationService;
import com.zhangyuan.modules.product.dto.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminProductControllerTest {

    private final ProductApplicationService productApplicationService = mock(ProductApplicationService.class);
    private final AdminProductController controller = new AdminProductController(productApplicationService);

    @Test
    void listGroups_shouldReturnGroups() {
        when(productApplicationService.listGroups()).thenReturn(List.of(
                new PlanGroupResponse(1L, "basic", "Basic", "desc", "enabled", 1, List.of())
        ));

        ApiResponse<List<PlanGroupResponse>> response = controller.listGroups();

        assertThat(response.code()).isZero();
        assertThat(response.data()).hasSize(1);
    }

    @Test
    void listPlans_shouldReturnPlans() {
        when(productApplicationService.listPlans()).thenReturn(List.of(
                new PlanResponse(1L, 1L, "pro", "Pro Plan", "desc", "Pro", "enabled", 2, List.of(), List.of())
        ));

        ApiResponse<List<PlanResponse>> response = controller.listPlans();

        assertThat(response.code()).isZero();
        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).code()).isEqualTo("pro");
    }

    @Test
    void createGroup_shouldReturnCreatedGroup() {
        var request = new CreatePlanGroupRequest("new-group", "New Group", "description", 1);
        var expected = new PlanGroupResponse(null, "new-group", "New Group", "description", "enabled", 1, List.of());
        when(productApplicationService.createGroup(request)).thenReturn(expected);

        ApiResponse<PlanGroupResponse> response = controller.createGroup(request);

        assertThat(response.code()).isZero();
        assertThat(response.data().code()).isEqualTo("new-group");
    }
}

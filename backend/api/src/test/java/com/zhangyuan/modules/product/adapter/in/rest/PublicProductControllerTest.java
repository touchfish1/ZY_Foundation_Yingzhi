package com.zhangyuan.modules.product.adapter.in.rest;

import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.product.application.service.ProductApplicationService;
import com.zhangyuan.modules.product.dto.PlanGroupResponse;
import com.zhangyuan.modules.product.dto.PlanResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PublicProductControllerTest {

    private final ProductApplicationService productApplicationService = mock(ProductApplicationService.class);
    private final PublicProductController controller = new PublicProductController(productApplicationService);

    @Test
    void listAvailablePlans_shouldReturnPlans() {
        when(productApplicationService.listPlans()).thenReturn(List.of(
                new PlanResponse(1L, 1L, "basic", "Basic Plan", "desc", "Basic", "enabled", 1, List.of(), List.of())
        ));

        ApiResponse<List<PlanResponse>> response = controller.listAvailablePlans();

        assertThat(response.code()).isZero();
        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).code()).isEqualTo("basic");
    }

    @Test
    void listAvailablePlans_whenEmpty_shouldReturnEmptyList() {
        when(productApplicationService.listPlans()).thenReturn(List.of());

        ApiResponse<List<PlanResponse>> response = controller.listAvailablePlans();

        assertThat(response.code()).isZero();
        assertThat(response.data()).isEmpty();
    }

    @Test
    void getGroup_shouldReturnGroup() {
        var group = new PlanGroupResponse(1L, "basic", "Basic", "desc", "enabled", 1, List.of());
        when(productApplicationService.getGroupByCode("basic")).thenReturn(group);

        ApiResponse<PlanGroupResponse> response = controller.getGroup("basic");

        assertThat(response.code()).isZero();
        assertThat(response.data().code()).isEqualTo("basic");
    }
}

package com.zhangyuan.modules.product;

import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.product.dto.CreateFeatureRequest;
import com.zhangyuan.modules.product.dto.CreatePlanGroupRequest;
import com.zhangyuan.modules.product.dto.CreatePlanRequest;
import com.zhangyuan.modules.product.dto.CreatePriceRequest;
import com.zhangyuan.modules.product.dto.FeatureResponse;
import com.zhangyuan.modules.product.dto.PlanGroupResponse;
import com.zhangyuan.modules.product.dto.PlanResponse;
import com.zhangyuan.modules.product.dto.PriceResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductAdminControllerTest {

    private final ProductService productService = mock(ProductService.class);
    private final ProductAdminController controller = new ProductAdminController(productService);

    @Test
    void listGroupsDelegatesToService() {
        when(productService.listGroups()).thenReturn(List.of(
                new PlanGroupResponse(1L, "g1", "G1", "desc", "enabled", 10, List.of())
        ));

        ApiResponse<List<PlanGroupResponse>> response = controller.listGroups();

        assertThat(response.data()).hasSize(1);
        verify(productService).listGroups();
    }

    @Test
    void listPlansDelegatesToService() {
        when(productService.listPlans()).thenReturn(List.of(
                new PlanResponse(1L, 1L, "pro", "Pro", "desc", "badge", "enabled", 10, List.of(), List.of())
        ));

        ApiResponse<List<PlanResponse>> response = controller.listPlans();

        assertThat(response.data()).hasSize(1);
        verify(productService).listPlans();
    }

    @Test
    void listPricesDelegatesToService() {
        when(productService.listPrices()).thenReturn(List.of(
                new PriceResponse(1L, "CNY", "monthly", BigDecimal.valueOf(29), null, "enabled")
        ));

        ApiResponse<List<PriceResponse>> response = controller.listPrices();

        assertThat(response.data()).hasSize(1);
        verify(productService).listPrices();
    }

    @Test
    void createGroupDelegatesToService() {
        CreatePlanGroupRequest request = new CreatePlanGroupRequest("new", "New", "desc", 5);
        PlanGroupResponse expected = new PlanGroupResponse(2L, "new", "New", "desc", "enabled", 5, List.of());
        when(productService.createGroup(request)).thenReturn(expected);

        ApiResponse<PlanGroupResponse> response = controller.createGroup(request);

        assertThat(response.data().code()).isEqualTo("new");
        verify(productService).createGroup(request);
    }

    @Test
    void createPlanDelegatesToService() {
        CreatePlanRequest request = new CreatePlanRequest(1L, "pro", "Pro", "desc", "badge", 10);
        PlanResponse expected = new PlanResponse(3L, 1L, "pro", "Pro", "desc", "badge", "enabled", 10, List.of(), List.of());
        when(productService.createPlan(request)).thenReturn(expected);

        ApiResponse<PlanResponse> response = controller.createPlan(request);

        assertThat(response.data().code()).isEqualTo("pro");
        verify(productService).createPlan(request);
    }

    @Test
    void createPriceDelegatesToService() {
        CreatePriceRequest request = new CreatePriceRequest(1L, "CNY", "monthly", BigDecimal.valueOf(29), null);
        PriceResponse expected = new PriceResponse(4L, "CNY", "monthly", BigDecimal.valueOf(29), null, "enabled");
        when(productService.createPrice(request)).thenReturn(expected);

        ApiResponse<PriceResponse> response = controller.createPrice(request);

        assertThat(response.data().amount()).isEqualByComparingTo(BigDecimal.valueOf(29));
        verify(productService).createPrice(request);
    }

    @Test
    void createFeatureDelegatesToService() {
        CreateFeatureRequest request = new CreateFeatureRequest(1L, "storage", "100GB", true, 1);
        FeatureResponse expected = new FeatureResponse(5L, "storage", "100GB", true, 1);
        when(productService.createFeature(request)).thenReturn(expected);

        ApiResponse<FeatureResponse> response = controller.createFeature(request);

        assertThat(response.data().featureName()).isEqualTo("storage");
        verify(productService).createFeature(request);
    }
}

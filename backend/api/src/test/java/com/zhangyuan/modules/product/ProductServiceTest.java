package com.zhangyuan.modules.product;

import com.zhangyuan.modules.product.domain.ProductFeature;
import com.zhangyuan.modules.product.domain.ProductPlan;
import com.zhangyuan.modules.product.domain.ProductPlanGroup;
import com.zhangyuan.modules.product.domain.ProductPrice;
import com.zhangyuan.modules.product.dto.CreateFeatureRequest;
import com.zhangyuan.modules.product.dto.CreatePlanGroupRequest;
import com.zhangyuan.modules.product.dto.CreatePlanRequest;
import com.zhangyuan.modules.product.dto.CreatePriceRequest;
import com.zhangyuan.modules.product.dto.FeatureResponse;
import com.zhangyuan.modules.product.dto.PlanGroupResponse;
import com.zhangyuan.modules.product.dto.PlanResponse;
import com.zhangyuan.modules.product.dto.PriceResponse;
import com.zhangyuan.modules.product.repository.ProductFeatureRepository;
import com.zhangyuan.modules.product.repository.ProductPlanGroupRepository;
import com.zhangyuan.modules.product.repository.ProductPlanRepository;
import com.zhangyuan.modules.product.repository.ProductPriceRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductServiceTest {

    private final ProductPlanGroupRepository planGroupRepository = mock(ProductPlanGroupRepository.class);
    private final ProductPlanRepository planRepository = mock(ProductPlanRepository.class);
    private final ProductPriceRepository priceRepository = mock(ProductPriceRepository.class);
    private final ProductFeatureRepository featureRepository = mock(ProductFeatureRepository.class);
    private final ProductService productService = new ProductService(planGroupRepository, planRepository, priceRepository, featureRepository);

    @Test
    void listGroupsReturnsAllGroups() {
        when(planGroupRepository.findAllByOrderBySortOrderAsc()).thenReturn(List.of(
                new ProductPlanGroup("api_plans", "API Plans", "desc", 10)
        ));
        when(planRepository.findByGroupIdOrderBySortOrderAsc(any())).thenReturn(List.of());

        List<PlanGroupResponse> groups = productService.listGroups();

        assertThat(groups).hasSize(1);
        assertThat(groups.getFirst().code()).isEqualTo("api_plans");
    }

    @Test
    void createGroupSavesAndReturns() {
        CreatePlanGroupRequest request = new CreatePlanGroupRequest("new_group", "New Group", "description", 5);
        when(planGroupRepository.existsByCode("new_group")).thenReturn(false);
        when(planGroupRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(planRepository.findByGroupIdOrderBySortOrderAsc(any())).thenReturn(List.of());

        PlanGroupResponse response = productService.createGroup(request);

        assertThat(response.code()).isEqualTo("new_group");
        assertThat(response.name()).isEqualTo("New Group");
        verify(planGroupRepository).save(any());
    }

    @Test
    void createGroupThrowsWhenCodeAlreadyExists() {
        CreatePlanGroupRequest request = new CreatePlanGroupRequest("existing", "Existing", "desc", 1);
        when(planGroupRepository.existsByCode("existing")).thenReturn(true);

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> productService.createGroup(request));
    }

    @Test
    void findGroupByCodeReturnsGroup() {
        when(planGroupRepository.findByCode("api_plans")).thenReturn(
                Optional.of(new ProductPlanGroup("api_plans", "API Plans", "desc", 10))
        );
        when(planRepository.findByGroupIdOrderBySortOrderAsc(any())).thenReturn(List.of());

        Optional<PlanGroupResponse> result = productService.findGroupByCode("api_plans");

        assertThat(result).isPresent();
        assertThat(result.get().code()).isEqualTo("api_plans");
    }

    @Test
    void findGroupByCodeReturnsEmptyWhenNotFound() {
        when(planGroupRepository.findByCode("missing")).thenReturn(Optional.empty());

        Optional<PlanGroupResponse> result = productService.findGroupByCode("missing");

        assertThat(result).isEmpty();
    }

    @Test
    void listPlansReturnsAllPlans() {
        when(planRepository.findAllByOrderBySortOrderAsc()).thenReturn(List.of());

        var plans = productService.listPlans();

        assertThat(plans).isNotNull();
        verify(planRepository).findAllByOrderBySortOrderAsc();
    }

    @Test
    void getGroupByCodeThrowsWhenNotFound() {
        when(planGroupRepository.findByCode("missing")).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> productService.getGroupByCode("missing"));
    }

    @Test
    void createPlanSavesAndReturns() {
        when(planGroupRepository.existsById(1L)).thenReturn(true);
        when(planRepository.existsByCode("pro")).thenReturn(false);
        when(planRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(priceRepository.findByPlanId(any())).thenReturn(List.of());
        when(featureRepository.findByPlanIdOrderBySortOrderAsc(any())).thenReturn(List.of());

        CreatePlanRequest request = new CreatePlanRequest(1L, "pro", "Pro", "desc", "badge", 10);
        PlanResponse response = productService.createPlan(request);

        assertThat(response.code()).isEqualTo("pro");
        assertThat(response.name()).isEqualTo("Pro");
        verify(planRepository).save(any());
    }

    @Test
    void createPlanThrowsWhenGroupNotFound() {
        when(planGroupRepository.existsById(99L)).thenReturn(false);

        CreatePlanRequest request = new CreatePlanRequest(99L, "pro", "Pro", "desc", null, 1);

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> productService.createPlan(request));
    }

    @Test
    void createPlanThrowsWhenCodeAlreadyExists() {
        when(planGroupRepository.existsById(1L)).thenReturn(true);
        when(planRepository.existsByCode("pro")).thenReturn(true);

        CreatePlanRequest request = new CreatePlanRequest(1L, "pro", "Pro", "desc", null, 1);

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> productService.createPlan(request));
    }

    @Test
    void createPriceSavesAndReturns() {
        when(planRepository.existsById(1L)).thenReturn(true);
        when(priceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CreatePriceRequest request = new CreatePriceRequest(1L, "CNY", "monthly", BigDecimal.valueOf(29), null);
        PriceResponse response = productService.createPrice(request);

        assertThat(response.currency()).isEqualTo("CNY");
        assertThat(response.amount()).isEqualByComparingTo(BigDecimal.valueOf(29));
        verify(priceRepository).save(any());
    }

    @Test
    void createPriceThrowsWhenPlanNotFound() {
        when(planRepository.existsById(99L)).thenReturn(false);

        CreatePriceRequest request = new CreatePriceRequest(99L, "CNY", "monthly", BigDecimal.valueOf(29), null);

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> productService.createPrice(request));
    }

    @Test
    void createFeatureSavesAndReturns() {
        when(planRepository.existsById(1L)).thenReturn(true);
        when(featureRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CreateFeatureRequest request = new CreateFeatureRequest(1L, "storage", "100GB", true, 1);
        FeatureResponse response = productService.createFeature(request);

        assertThat(response.featureName()).isEqualTo("storage");
        verify(featureRepository).save(any());
    }

    @Test
    void createFeatureThrowsWhenPlanNotFound() {
        when(planRepository.existsById(99L)).thenReturn(false);

        CreateFeatureRequest request = new CreateFeatureRequest(99L, "storage", "100GB", true, 1);

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> productService.createFeature(request));
    }

    @Test
    void listPricesReturnsAllPrices() {
        when(priceRepository.findAllByOrderByPlanIdAsc()).thenReturn(List.of(
                new ProductPrice(1L, "CNY", "monthly", BigDecimal.valueOf(29), null)
        ));

        List<PriceResponse> prices = productService.listPrices();

        assertThat(prices).hasSize(1);
        assertThat(prices.getFirst().currency()).isEqualTo("CNY");
        verify(priceRepository).findAllByOrderByPlanIdAsc();
    }
}

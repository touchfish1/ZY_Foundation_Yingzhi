package com.zhangyuan.modules.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangyuan.modules.order.domain.OrderMain;
import com.zhangyuan.modules.order.dto.CreateOrderRequest;
import com.zhangyuan.modules.order.dto.OrderResponse;
import com.zhangyuan.modules.order.repository.OrderMainRepository;
import com.zhangyuan.modules.product.domain.ProductPlan;
import com.zhangyuan.modules.product.domain.ProductPrice;
import com.zhangyuan.modules.product.repository.ProductPlanRepository;
import com.zhangyuan.modules.product.repository.ProductPriceRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    private final OrderMainRepository orderMainRepository = mock(OrderMainRepository.class);
    private final ProductPlanRepository productPlanRepository = mock(ProductPlanRepository.class);
    private final ProductPriceRepository productPriceRepository = mock(ProductPriceRepository.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OrderService orderService = new OrderService(orderMainRepository, productPlanRepository, productPriceRepository, objectMapper);

    @Test
    void createOrderSavesAndReturns() {
        CreateOrderRequest request = new CreateOrderRequest("pro", "monthly", "CNY");
        ProductPlan plan = mock(ProductPlan.class);
        when(plan.getId()).thenReturn(1L);
        when(plan.getGroupId()).thenReturn(1L);
        when(plan.getCode()).thenReturn("pro");
        when(plan.getName()).thenReturn("Pro");
        when(plan.getDescription()).thenReturn("desc");
        when(plan.getBadge()).thenReturn("Hot");
        when(plan.getStatus()).thenReturn("enabled");
        ProductPrice price = mock(ProductPrice.class);
        when(price.getId()).thenReturn(1L);
        when(price.getPlanId()).thenReturn(1L);
        when(price.getBillingCycle()).thenReturn("monthly");
        when(price.getCurrency()).thenReturn("CNY");
        when(price.getAmount()).thenReturn(BigDecimal.valueOf(29));
        when(price.getOriginalAmount()).thenReturn(BigDecimal.valueOf(39));
        when(price.getStatus()).thenReturn("enabled");
        when(productPlanRepository.findByCode("pro")).thenReturn(Optional.of(plan));
        when(productPriceRepository.findFirstByPlanIdAndBillingCycleAndCurrencyAndStatus(any(), eq("monthly"), eq("CNY"), eq("enabled")))
                .thenReturn(Optional.of(price));
        when(orderMainRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.create(request);

        assertThat(response.amount()).isEqualByComparingTo(BigDecimal.valueOf(29));
        assertThat(response.currency()).isEqualTo("CNY");
        assertThat(response.status()).isEqualTo("pending");
        verify(orderMainRepository).save(any());
    }

    @Test
    void createOrderThrowsWhenPlanNotFound() {
        CreateOrderRequest request = new CreateOrderRequest("invalid", "monthly", "CNY");
        when(productPlanRepository.findByCode("invalid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.create(request))
                .hasMessageContaining("Product plan not found");
    }

    @Test
    void createOrderThrowsWhenPlanDisabled() {
        CreateOrderRequest request = new CreateOrderRequest("disabled_plan", "monthly", "CNY");
        ProductPlan plan = mock(ProductPlan.class);
        when(plan.getStatus()).thenReturn("disabled");
        when(productPlanRepository.findByCode("disabled_plan")).thenReturn(Optional.of(plan));

        assertThatThrownBy(() -> orderService.create(request))
                .hasMessageContaining("Product plan not found");
    }

    @Test
    void createOrderThrowsWhenPriceNotFound() {
        CreateOrderRequest request = new CreateOrderRequest("pro", "yearly", "CNY");
        ProductPlan plan = mock(ProductPlan.class);
        when(plan.getId()).thenReturn(1L);
        when(plan.getStatus()).thenReturn("enabled");
        when(productPlanRepository.findByCode("pro")).thenReturn(Optional.of(plan));
        when(productPriceRepository.findFirstByPlanIdAndBillingCycleAndCurrencyAndStatus(any(), eq("yearly"), eq("CNY"), eq("enabled")))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.create(request))
                .hasMessageContaining("Product price not found");
    }

    @Test
    void getReturnsOrder() {
        OrderMain order = mock(OrderMain.class);
        when(order.getOrderNo()).thenReturn("ORD123");
        when(order.getPlanId()).thenReturn(1L);
        when(order.getPriceId()).thenReturn(1L);
        when(order.getAmount()).thenReturn(BigDecimal.valueOf(29));
        when(order.getCurrency()).thenReturn("CNY");
        when(order.getStatus()).thenReturn("paid");
        when(order.getSnapshotJson()).thenReturn("{}");
        when(order.getCreatedAt()).thenReturn(null);
        when(order.getPaidAt()).thenReturn(null);
        when(orderMainRepository.findByOrderNo("ORD123")).thenReturn(Optional.of(order));

        OrderResponse response = orderService.get("ORD123");

        assertThat(response.orderNo()).isEqualTo("ORD123");
        assertThat(response.status()).isEqualTo("paid");
    }

    @Test
    void getThrowsWhenOrderNotFound() {
        when(orderMainRepository.findByOrderNo("MISSING")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.get("MISSING"))
                .hasMessageContaining("Order not found");
    }

    @Test
    void listOrdersReturnsAll() {
        when(orderMainRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

        var orders = orderService.listOrders();

        assertThat(orders).isNotNull();
        verify(orderMainRepository).findAllByOrderByCreatedAtDesc();
    }
}

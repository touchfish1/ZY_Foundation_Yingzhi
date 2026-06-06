package com.zhangyuan.order.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangyuan.order.client.ProductServiceClient;
import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.order.domain.model.OrderNumber;
import com.zhangyuan.order.domain.repository.OrderRepository;
import com.zhangyuan.order.domain.service.OrderDomainService;
import com.zhangyuan.order.dto.CreateOrderRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderApplicationServiceTest {

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final OrderDomainService orderDomainService = new OrderDomainService();
    private final ProductServiceClient productClient = mock(ProductServiceClient.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private OrderApplicationService service;

    @BeforeEach
    void setUp() {
        service = new OrderApplicationService(orderRepository, orderDomainService, productClient, objectMapper);
    }

    @Test
    void createOrder_planNotFound_throws() {
        when(productClient.getPlanGroup("NONEXISTENT")).thenReturn(
            new ApiResponse<>(404, "Plan not found", null));

        assertThatThrownBy(() -> service.createOrder(
            new CreateOrderRequest("NONEXISTENT", "monthly", "CNY")))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Product plan not found");
    }

    @Test
    void getOrder_notFound_throws() {
        when(orderRepository.findByOrderNo(new OrderNumber("NONEXISTENT"))).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getOrder("NONEXISTENT"))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Order not found");
    }
}

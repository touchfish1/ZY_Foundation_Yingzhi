package com.zhangyuan.modules.order;

import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.order.dto.OrderResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderAdminControllerTest {

    private final OrderService orderService = mock(OrderService.class);
    private final OrderAdminController controller = new OrderAdminController(orderService);

    @Test
    void listOrdersDelegatesToService() {
        OrderResponse expected = new OrderResponse("ORD1", 1L, 1L, BigDecimal.valueOf(29), "CNY", "paid",
                "{}", Instant.parse("2026-01-01T00:00:00Z"), null);
        when(orderService.listOrders()).thenReturn(List.of(expected));

        ApiResponse<List<OrderResponse>> response = controller.listOrders();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().orderNo()).isEqualTo("ORD1");
        verify(orderService).listOrders();
    }
}

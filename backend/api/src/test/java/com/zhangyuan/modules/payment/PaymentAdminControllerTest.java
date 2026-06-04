package com.zhangyuan.modules.payment;

import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.payment.dto.PaymentResponse;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentAdminControllerTest {

    private final PaymentService paymentService = mock(PaymentService.class);
    private final PaymentAdminController controller = new PaymentAdminController(paymentService);

    @Test
    void listPaymentsDelegatesToService() {
        PaymentResponse expected = new PaymentResponse("PAY1", "ORD1", "paid", Instant.parse("2026-01-01T00:00:00Z"));
        when(paymentService.listPayments()).thenReturn(List.of(expected));

        ApiResponse<List<PaymentResponse>> response = controller.listPayments();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().paymentNo()).isEqualTo("PAY1");
        verify(paymentService).listPayments();
    }
}

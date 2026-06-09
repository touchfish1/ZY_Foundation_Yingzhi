package com.zhangyuan.modules.payment.adapter.in.rest;

import com.zhangyuan.common.exception.NotFoundException;
import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.payment.application.service.PaymentApplicationService;
import com.zhangyuan.modules.payment.dto.CheckoutRequest;
import com.zhangyuan.modules.payment.dto.CheckoutResponse;
import com.zhangyuan.modules.payment.dto.PaymentResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PaymentPublicControllerTest {

    private final PaymentApplicationService paymentApplicationService = mock(PaymentApplicationService.class);
    private final PaymentPublicController controller = new PaymentPublicController(paymentApplicationService);

    @Test
    void getPayment_shouldReturn200() {
        PaymentResponse expected = new PaymentResponse("PAY001", "ORD001", "PENDING", null, "mock", BigDecimal.TEN, "CNY", Instant.now());
        when(paymentApplicationService.getPayment("PAY001")).thenReturn(expected);

        ApiResponse<PaymentResponse> response = controller.get("PAY001");

        assertThat(response.code()).isZero();
        assertThat(response.data().paymentNo()).isEqualTo("PAY001");
    }

    @Test
    void getPayment_whenNotFound_shouldPropagateException() {
        when(paymentApplicationService.getPayment("NONEXISTENT"))
                .thenThrow(new NotFoundException("Payment not found: NONEXISTENT"));

        assertThatThrownBy(() -> controller.get("NONEXISTENT"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Payment not found");
    }

    @Test
    void checkout_withMockChannel_shouldReturnCheckout() {
        CheckoutRequest request = new CheckoutRequest("ORD001", "mock");
        CheckoutResponse expected = new CheckoutResponse("PAY001", "PENDING", "/api/payments/mock/PAY001/success", null);
        when(paymentApplicationService.checkout(request)).thenReturn(expected);

        ApiResponse<CheckoutResponse> response = controller.checkout(request);

        assertThat(response.code()).isZero();
        assertThat(response.data().mockPayUrl()).contains("/api/payments/mock/");
    }

    @Test
    void checkout_withInvalidChannel_shouldPropagateException() {
        CheckoutRequest request = new CheckoutRequest("ORD001", "invalid");
        when(paymentApplicationService.checkout(request))
                .thenThrow(new IllegalArgumentException("Unsupported payment channel: invalid"));

        assertThatThrownBy(() -> controller.checkout(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported payment channel");
    }
}

package com.zhangyuan.modules.payment.adapter.out.persistence;

import com.zhangyuan.modules.payment.adapter.out.persistence.PaymentTransactionEntity;
import com.zhangyuan.modules.payment.adapter.out.persistence.PaymentTransactionJpaRepository;
import com.zhangyuan.modules.payment.domain.model.Payment;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JpaPaymentRepositoryTest {

    private final PaymentTransactionJpaRepository jpaRepository = mock(PaymentTransactionJpaRepository.class);
    private final JpaPaymentRepository repository = new JpaPaymentRepository(jpaRepository);

    @Test
    void findByPaymentNoDelegates() {
        PaymentTransactionEntity entity = new PaymentTransactionEntity(
                "PAY123", 1L, "mock", BigDecimal.valueOf(29), "CNY", "{}");
        when(jpaRepository.findByPaymentNo("PAY123")).thenReturn(Optional.of(entity));

        Optional<Payment> result = repository.findByPaymentNo("PAY123");

        assertThat(result).isPresent();
        assertThat(result.get().getPaymentNo()).isEqualTo("PAY123");
    }
}

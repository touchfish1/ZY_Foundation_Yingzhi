package com.zhangyuan.order.application.service;

import com.zhangyuan.order.domain.model.UserSubscription;
import com.zhangyuan.order.domain.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class SubscriptionQueryServiceTest {
    private final SubscriptionRepository subscriptionRepository = mock(SubscriptionRepository.class);
    private SubscriptionQueryService service;

    @BeforeEach
    void setUp() { service = new SubscriptionQueryService(subscriptionRepository); }

    @Test
    void findActiveByUser_returnsSubscription() {
        var sub = new UserSubscription(1L, "premium", "Premium", 30);
        when(subscriptionRepository.findByUserIdAndActive(1L)).thenReturn(Optional.of(sub));

        var result = service.findActiveByUser(1L);
        assertThat(result).isPresent();
        assertThat(result.get().getPlanCode()).isEqualTo("premium");
    }

    @Test
    void findActiveByUser_noSubscription_returnsEmpty() {
        when(subscriptionRepository.findByUserIdAndActive(1L)).thenReturn(Optional.empty());
        assertThat(service.findActiveByUser(1L)).isEmpty();
    }

    @Test
    void findByUser_returnsAll() {
        when(subscriptionRepository.findByUserId(1L)).thenReturn(List.of(
            new UserSubscription(1L, "premium", "Premium", 30),
            new UserSubscription(1L, "basic", "Basic", 15)
        ));
        assertThat(service.findByUser(1L)).hasSize(2);
    }
}

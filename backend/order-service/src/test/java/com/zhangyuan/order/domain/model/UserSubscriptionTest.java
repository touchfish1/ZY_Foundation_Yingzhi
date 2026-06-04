package com.zhangyuan.order.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.*;

class UserSubscriptionTest {

    @Test
    void createSubscription() {
        var sub = new UserSubscription(1L, "premium", "Premium Plan", 30);

        assertThat(sub.getUserId()).isEqualTo(1L);
        assertThat(sub.getStatus()).isEqualTo("active");
        assertThat(sub.getExpiresAt()).isAfter(Instant.now());
    }

    @Test
    void extend_activeSubscription() {
        var sub = new UserSubscription(1L, "premium", "Premium Plan", 30);
        Instant original = sub.getExpiresAt();

        sub.extend(15);

        assertThat(sub.getExpiresAt()).isAfter(original);
    }

    @Test
    void extend_expiredSubscription() {
        var sub = new UserSubscription(1L, "premium", "Premium Plan", -1); // expires in past
        Instant pastExpiry = sub.getExpiresAt();

        sub.extend(30);

        assertThat(sub.getExpiresAt()).isAfter(pastExpiry);
        assertThat(sub.getStatus()).isEqualTo("active");
    }

    @Test
    void isActive_returnsTrue_whenActiveAndNotExpired() {
        var sub = new UserSubscription(1L, "premium", "Premium Plan", 30);

        assertThat(sub.isActive()).isTrue();
    }

    @Test
    void isActive_returnsFalse_whenSuspended() {
        var sub = new UserSubscription(1L, "premium", "Premium Plan", 30);
        sub.suspend();

        assertThat(sub.isActive()).isFalse();
    }

    @Test
    void isActive_returnsFalse_whenExpired() {
        var sub = new UserSubscription(1L, "premium", "Premium Plan", -1);

        assertThat(sub.isActive()).isFalse();
    }

    @Test
    void suspend_and_activate() {
        var sub = new UserSubscription(1L, "premium", "Premium Plan", 30);
        sub.suspend();

        assertThat(sub.isActive()).isFalse();

        sub.activate();

        assertThat(sub.isActive()).isTrue();
    }
}

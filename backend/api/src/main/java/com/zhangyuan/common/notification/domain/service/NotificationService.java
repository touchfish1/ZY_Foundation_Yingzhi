package com.zhangyuan.common.notification.domain.service;

import com.zhangyuan.common.notification.domain.model.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final List<NotificationSender> senders;

    public NotificationService(List<NotificationSender> senders) {
        this.senders = senders;
    }

    public void notify(String type, Long userId, String email, Map<String, Object> payload) {
        NotificationEvent event = new NotificationEvent(type, userId, email, payload);
        for (NotificationSender sender : senders) {
            try {
                sender.send(event);
            } catch (Exception e) {
                log.warn("Notification send failed via {}: {}", sender.getClass().getSimpleName(), e.getMessage());
            }
        }
    }

    public void notifyQuotaWarning(Long userId, String email, long used, long limit) {
        notify("quota_warning", userId, email, Map.of(
                "title", "Quota Warning",
                "used", used,
                "limit", limit,
                "percent", String.format("%.0f%%", (double) used / limit * 100)
        ));
    }

    public void notifySubscriptionExpiring(Long userId, String email, String planName, String expiresAt) {
        notify("subscription_expiring", userId, email, Map.of(
                "title", "Subscription Expiring Soon",
                "planName", planName,
                "expiresAt", expiresAt
        ));
    }

    public void notifyPaymentSuccess(Long userId, String email, String orderNo, String amount) {
        notify("payment_success", userId, email, Map.of(
                "title", "Payment Successful",
                "orderNo", orderNo,
                "amount", amount
        ));
    }
}

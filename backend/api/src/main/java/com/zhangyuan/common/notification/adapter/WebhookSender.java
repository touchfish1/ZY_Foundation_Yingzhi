package com.zhangyuan.common.notification.adapter;

import com.zhangyuan.common.notification.domain.model.NotificationEvent;
import com.zhangyuan.common.notification.domain.service.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class WebhookSender implements NotificationSender {
    private static final Logger log = LoggerFactory.getLogger(WebhookSender.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public boolean supports(String channel) {
        return "webhook".equals(channel);
    }

    @Override
    public void send(NotificationEvent event) {
        String webhookUrl = (String) event.getPayload().get("webhookUrl");
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.debug("No webhook URL configured for userId={}", event.getUserId());
            return;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> body = Map.of(
                    "type", event.getType(),
                    "userId", event.getUserId(),
                    "timestamp", event.getCreatedAt().toString(),
                    "payload", event.getPayload()
            );
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(webhookUrl, entity, String.class);
            log.info("Webhook sent to {}: type={}", webhookUrl, event.getType());
        } catch (Exception e) {
            log.error("Failed to send webhook to {}: {}", webhookUrl, e.getMessage());
        }
    }
}

package com.zhangyuan.common.notification.adapter;

import com.zhangyuan.common.notification.domain.model.NotificationEvent;
import com.zhangyuan.common.notification.domain.service.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailSender implements NotificationSender {
    private static final Logger log = LoggerFactory.getLogger(EmailSender.class);
    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

    public EmailSender(JavaMailSender mailSender, MailProperties mailProperties) {
        this.mailSender = mailSender;
        this.mailProperties = mailProperties;
    }

    @Override
    public boolean supports(String channel) {
        return "email".equals(channel);
    }

    @Override
    public void send(NotificationEvent event) {
        if (event.getEmail() == null || event.getEmail().isBlank()) {
            log.warn("No email address for userId={}", event.getUserId());
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(mailProperties.getUsername());
            msg.setTo(event.getEmail());
            msg.setSubject(buildSubject(event));
            msg.setText(buildBody(event));
            mailSender.send(msg);
            log.info("Email sent to {}: type={}", event.getEmail(), event.getType());
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", event.getEmail(), e.getMessage());
        }
    }

    private String buildSubject(NotificationEvent event) {
        Object title = event.getPayload().get("title");
        return title != null ? "[Zhangyuan AI] " + title : "[Zhangyuan AI] Notification";
    }

    private String buildBody(NotificationEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("Notification: ").append(event.getType()).append("\n\n");
        event.getPayload().forEach((k, v) -> sb.append(k).append(": ").append(v).append("\n"));
        sb.append("\n— Zhangyuan AI");
        return sb.toString();
    }
}

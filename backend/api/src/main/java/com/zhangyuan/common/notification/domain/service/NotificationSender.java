package com.zhangyuan.common.notification.domain.service;

import com.zhangyuan.common.notification.domain.model.NotificationEvent;

public interface NotificationSender {
    boolean supports(String channel);
    void send(NotificationEvent event);
}

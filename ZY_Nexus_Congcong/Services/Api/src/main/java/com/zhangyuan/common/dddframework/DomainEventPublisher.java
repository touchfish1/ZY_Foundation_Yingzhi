package com.zhangyuan.common.dddframework;

import java.util.List;

public interface DomainEventPublisher {

    void publish(DomainEvent event);

    void publishAll(List<DomainEvent> events);
}


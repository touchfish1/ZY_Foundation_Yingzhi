package com.zhangyuan.common.ddd;

import java.util.List;

public interface DomainEventPublisher {

    void publish(DomainEvent event);

    void publishAll(List<DomainEvent> events);
}

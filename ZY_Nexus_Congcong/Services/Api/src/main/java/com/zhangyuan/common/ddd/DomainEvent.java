package com.zhangyuan.common.ddd;

import java.time.Instant;

public interface DomainEvent {

    default Instant occurredAt() {
        return Instant.now();
    }
}

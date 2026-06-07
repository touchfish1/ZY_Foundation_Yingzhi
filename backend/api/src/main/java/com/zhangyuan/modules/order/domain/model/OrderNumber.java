package com.zhangyuan.modules.order.domain.model;

import com.zhangyuan.common.dddframework.ValueObject;
import java.time.Instant;
import java.util.UUID;

public record OrderNumber(String value) implements ValueObject {

    public OrderNumber {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Order number must not be blank");
        }
    }

    public static OrderNumber generate() {
        return new OrderNumber("ORD" + Instant.now().toEpochMilli()
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase());
    }

    @Override
    public String toString() {
        return value;
    }
}

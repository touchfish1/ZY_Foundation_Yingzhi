package com.zhangyuan.modules.order.domain.model;

import com.zhangyuan.common.dddframework.AggregateRoot;
import java.math.BigDecimal;
import java.time.Instant;

public class Order extends AggregateRoot<Long> {

    private OrderNumber orderNo;
    private Long planId;
    private Long priceId;
    private BigDecimal amount;
    private String currency;
    private OrderStatus status;
    private String snapshotJson;
    private Instant createdAt;
    private Instant paidAt;
    private Instant cancelledAt;

    public Order(OrderNumber orderNo, Long planId, Long priceId, BigDecimal amount, String currency, String snapshotJson) {
        this.orderNo = orderNo;
        this.planId = planId;
        this.priceId = priceId;
        this.amount = amount;
        this.currency = currency;
        this.snapshotJson = snapshotJson;
        this.status = OrderStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public void markPaid() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Only pending orders can be paid. Current status: " + status);
        }
        this.status = OrderStatus.PAID;
        this.paidAt = Instant.now();
    }

    public void cancel() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Only pending orders can be cancelled. Current status: " + status);
        }
        this.status = OrderStatus.CANCELLED;
        this.cancelledAt = Instant.now();
    }

    /**
     * 从持久化数据重建订单领域对象，保留数据库中的完整时间戳。
     * 与公开构造器的区别：不调用 markPaid()/cancel() 覆盖时间戳。
     */
    public static Order reconstitute(OrderNumber orderNo, Long planId, Long priceId,
                                      BigDecimal amount, String currency, String snapshotJson,
                                      OrderStatus status, Instant createdAt,
                                      Instant paidAt, Instant cancelledAt) {
        Order order = new Order(orderNo, planId, priceId, amount, currency, snapshotJson);
        order.status = status;
        order.createdAt = createdAt;
        order.paidAt = paidAt;
        order.cancelledAt = cancelledAt;
        return order;
    }

    public boolean isPaid() { return status == OrderStatus.PAID; }
    public boolean isPending() { return status == OrderStatus.PENDING; }

    public OrderNumber getOrderNo() { return orderNo; }
    public Long getPlanId() { return planId; }
    public Long getPriceId() { return priceId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public OrderStatus getStatus() { return status; }
    public String getSnapshotJson() { return snapshotJson; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getPaidAt() { return paidAt; }
    public Instant getCancelledAt() { return cancelledAt; }
}

package com.zhangyuan.common.dddframework;

public abstract class AggregateRoot<T> extends Entity<T> {

    protected AggregateRoot() {
        super();
    }

    protected AggregateRoot(T id) {
        super(id);
    }
}

package com.zhangyuan.common.dddframework;

import java.util.Objects;

public abstract class Entity<T> {

    private T id;

    protected Entity() {
        // For creating entities without an assigned identity (e.g., before persistence)
    }

    protected Entity(T id) {
        this.id = id;
    }

    public T getId() {
        return id;
    }

    public void setId(T id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity<?> entity = (Entity<?>) o;
        // If both ids are non-null, compare by identity
        if (id != null && entity.id != null) {
            return Objects.equals(id, entity.id);
        }
        // Otherwise fall back to reference identity
        return false;
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : super.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id=" + id + "}";
    }
}

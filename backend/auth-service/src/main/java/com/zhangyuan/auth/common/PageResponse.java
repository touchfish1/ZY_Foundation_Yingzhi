package com.zhangyuan.auth.common;

import org.springframework.data.domain.Page;
import java.util.List;

public record PageResponse<T>(List<T> items, int page, int pageSize, long total) {

    public static <T> PageResponse<T> of(List<T> items, int page, int pageSize, long total) {
        return new PageResponse<>(items, page, pageSize, total);
    }

    public static <T, E> PageResponse<T> from(Page<E> page, List<T> items) {
        return new PageResponse<>(items, page.getNumber() + 1, page.getSize(), page.getTotalElements());
    }
}

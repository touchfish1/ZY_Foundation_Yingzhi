package com.zhangyuan.common.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(List<T> items, int page, int pageSize, long total) {

    public static <T> PageResponse<T> of(List<T> items, int page, int pageSize, long total) {
        return new PageResponse<>(items, page, pageSize, total);
    }

    /**
     * Create a PageResponse from a Spring Data Page.
     * Converts the page number from 0-based (Spring Data) to 1-based.
     */
    public static <T> PageResponse<T> from(Page<T> springPage) {
        return new PageResponse<>(springPage.getContent(), springPage.getNumber() + 1,
                springPage.getSize(), springPage.getTotalElements());
    }

    /**
     * Create a PageResponse from a Spring Data Page and a mapped item list.
     * Converts the page number from 0-based (Spring Data) to 1-based.
     */
    public static <T, R> PageResponse<R> from(Page<T> springPage, List<R> items) {
        return new PageResponse<>(items, springPage.getNumber() + 1, springPage.getSize(), springPage.getTotalElements());
    }
}

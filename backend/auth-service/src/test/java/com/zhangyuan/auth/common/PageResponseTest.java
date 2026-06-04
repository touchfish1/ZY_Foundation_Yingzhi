package com.zhangyuan.auth.common;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class PageResponseTest {

    @Test
    void ofCreatesCorrectResponse() {
        var items = List.of("a", "b");
        var resp = PageResponse.of(items, 1, 20, 100);
        assertThat(resp.items()).hasSize(2);
        assertThat(resp.page()).isEqualTo(1);
        assertThat(resp.pageSize()).isEqualTo(20);
        assertThat(resp.total()).isEqualTo(100);
    }

    @Test
    void fromSpringPageConvertsCorrectly() {
        var pageable = PageRequest.of(0, 10);
        var springPage = new PageImpl<>(List.of("x", "y"), pageable, 50);
        var resp = PageResponse.from(springPage, springPage.getContent());
        assertThat(resp.items()).hasSize(2);
        assertThat(resp.page()).isEqualTo(1); // 0-based → 1-based
        assertThat(resp.pageSize()).isEqualTo(10);
        assertThat(resp.total()).isEqualTo(50);
    }

    @Test
    void emptyPage() {
        var pageable = PageRequest.of(0, 20);
        var springPage = new PageImpl<>(List.of(), pageable, 0);
        var resp = PageResponse.from(springPage, springPage.getContent());
        assertThat(resp.items()).isEmpty();
        assertThat(resp.total()).isZero();
    }

    @Test
    void pageResponseWithNegativePage() {
        var resp = PageResponse.of(List.of(), -1, 20, 0);
        assertThat(resp.items()).isEmpty();
        assertThat(resp.total()).isZero();
    }

    @Test
    void pageResponseWithMaxPageSize() {
        var items = new ArrayList<String>();
        for (int i = 0; i < 1000; i++) items.add("item" + i);
        var resp = PageResponse.of(items, 1, 1000, 10000);
        assertThat(resp.items()).hasSize(1000);
    }
}

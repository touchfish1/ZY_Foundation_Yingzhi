package com.zhangyuan.auth.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SortItem(
        @NotNull Long id,
        @NotNull @Min(0) Integer sortOrder
) {}

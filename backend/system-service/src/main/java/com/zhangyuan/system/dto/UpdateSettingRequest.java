package com.zhangyuan.system.dto;

import jakarta.validation.constraints.Size;

public record UpdateSettingRequest(
        @Size(max = 65535) String value
) {
}

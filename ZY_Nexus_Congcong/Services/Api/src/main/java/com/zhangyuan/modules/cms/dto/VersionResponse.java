package com.zhangyuan.modules.cms.dto;

import java.time.Instant;

public record VersionResponse(Long id, Integer versionNo, Instant createdAt, String remark) {
}

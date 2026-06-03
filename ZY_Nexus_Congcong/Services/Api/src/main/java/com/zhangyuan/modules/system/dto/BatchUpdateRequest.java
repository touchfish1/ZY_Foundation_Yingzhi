package com.zhangyuan.modules.system.dto;

import java.util.Map;

public record BatchUpdateRequest(Map<String, String> settings) {
}

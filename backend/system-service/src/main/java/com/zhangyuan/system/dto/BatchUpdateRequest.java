package com.zhangyuan.system.dto;

import java.util.Map;

public record BatchUpdateRequest(Map<String, String> settings) {
}

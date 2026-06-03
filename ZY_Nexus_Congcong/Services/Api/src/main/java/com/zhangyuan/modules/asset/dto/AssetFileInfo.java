package com.zhangyuan.modules.asset.dto;

public record AssetFileInfo(Long id, String url, String originalName, String contentType, Long sizeBytes) {
}

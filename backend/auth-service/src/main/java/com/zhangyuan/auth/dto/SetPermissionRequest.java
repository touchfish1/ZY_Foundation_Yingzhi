package com.zhangyuan.auth.dto;

import java.util.List;

public record SetPermissionRequest(List<Long> permissionIds) {}

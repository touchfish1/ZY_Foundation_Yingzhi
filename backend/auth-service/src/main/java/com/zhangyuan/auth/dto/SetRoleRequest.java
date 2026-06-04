package com.zhangyuan.auth.dto;

import java.util.List;

public record SetRoleRequest(List<Long> roleIds) {}

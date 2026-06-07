package com.zhangyuan.common.accesslog.domain.repository;

import com.zhangyuan.common.accesslog.domain.model.AccessLog;

import java.time.Instant;
import java.util.List;

public interface AccessLogRepository {

    AccessLog save(AccessLog log);

    List<AccessLog> query(String method, String path, Integer status, Long userId,
                          Instant startTime, Instant endTime, int page, int pageSize);

    long count(String method, String path, Integer status, Long userId,
               Instant startTime, Instant endTime);
}

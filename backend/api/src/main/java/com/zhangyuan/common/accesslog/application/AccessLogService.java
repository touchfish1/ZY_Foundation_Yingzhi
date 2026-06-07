package com.zhangyuan.common.accesslog.application;

import com.zhangyuan.common.accesslog.domain.model.AccessLog;
import com.zhangyuan.common.accesslog.domain.repository.AccessLogRepository;
import com.zhangyuan.common.accesslog.dto.AccessLogResponse;
import com.zhangyuan.common.response.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class AccessLogService {

    private static final Logger log = LoggerFactory.getLogger(AccessLogService.class);

    private final AccessLogRepository accessLogRepository;

    public AccessLogService(AccessLogRepository accessLogRepository) {
        this.accessLogRepository = accessLogRepository;
    }

    @Transactional
    public void record(String requestMethod, String requestPath, Integer responseStatus,
                       Long userId, String username, String ipAddress, String userAgent,
                       Long durationMs) {
        AccessLog accessLog = new AccessLog(
                requestMethod, requestPath, responseStatus,
                userId, username, ipAddress, userAgent, durationMs
        );
        accessLogRepository.save(accessLog);
        log.debug("Access log recorded: {} {} {} - {}ms", requestMethod, requestPath, responseStatus, durationMs);
    }

    @Transactional(readOnly = true)
    public PageResponse<AccessLogResponse> query(String method, String path, Integer status, Long userId,
                                                   Instant startTime, Instant endTime, int page, int pageSize) {
        List<AccessLog> items = accessLogRepository.query(method, path, status, userId,
                startTime, endTime, page, pageSize);
        long total = accessLogRepository.count(method, path, status, userId, startTime, endTime);
        List<AccessLogResponse> responses = items.stream()
                .map(this::toResponse)
                .toList();
        return PageResponse.of(responses, page, pageSize, total);
    }

    private AccessLogResponse toResponse(AccessLog log) {
        return new AccessLogResponse(
                log.getId(),
                log.getRequestMethod(),
                log.getRequestPath(),
                log.getResponseStatus(),
                log.getUserId(),
                log.getUsername(),
                log.getIpAddress(),
                log.getUserAgent(),
                log.getDurationMs(),
                log.getCreatedAt()
        );
    }
}

package com.zhangyuan.common.accesslog.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface AccessLogJpaRepository extends JpaRepository<AccessLogEntity, Long> {

    @Query("""
            SELECT a FROM AccessLogEntity a
            WHERE (:method IS NULL OR a.requestMethod = :method)
            AND (:path IS NULL OR a.requestPath LIKE %:path%)
            AND (:status IS NULL OR a.responseStatus = :status)
            AND (:userId IS NULL OR a.userId = :userId)
            AND a.createdAt >= COALESCE(:startTime, a.createdAt)
            AND a.createdAt <= COALESCE(:endTime, a.createdAt)
            ORDER BY a.createdAt DESC
            """)
    Page<AccessLogEntity> query(@Param("method") String method,
                                @Param("path") String path,
                                @Param("status") Integer status,
                                @Param("userId") Long userId,
                                @Param("startTime") Instant startTime,
                                @Param("endTime") Instant endTime,
                                Pageable pageable);
}

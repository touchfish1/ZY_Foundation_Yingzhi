package com.zhangyuan.common.operationlog.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface OperationLogJpaRepository extends JpaRepository<OperationLogEntity, Long> {

    @Query("""
            SELECT o FROM OperationLogEntity o
            WHERE (:operatorId IS NULL OR o.operatorId = :operatorId)
            AND (:resourceType IS NULL OR o.resourceType = :resourceType)
            AND (:operationType IS NULL OR o.operationType = :operationType)
            AND o.createdAt >= COALESCE(:startTime, o.createdAt)
            AND o.createdAt <= COALESCE(:endTime, o.createdAt)
            ORDER BY o.createdAt DESC
            """)
    Page<OperationLogEntity> query(@Param("operatorId") Long operatorId,
                                   @Param("resourceType") String resourceType,
                                   @Param("operationType") String operationType,
                                   @Param("startTime") Instant startTime,
                                   @Param("endTime") Instant endTime,
                                   Pageable pageable);
}

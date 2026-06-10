package com.zhangyuan.modules.ai.config;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PlanModelAccessRepository extends JpaRepository<PlanModelAccess, Long> {
    List<PlanModelAccess> findByPlanCode(String planCode);
    Optional<PlanModelAccess> findByPlanCodeAndModelName(String planCode, String modelName);
}

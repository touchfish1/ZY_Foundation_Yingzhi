package com.zhangyuan.modules.ai.config;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ModelRouteRepository extends JpaRepository<ModelRouteConfig, Long> {
    Optional<ModelRouteConfig> findByModelName(String modelName);
}

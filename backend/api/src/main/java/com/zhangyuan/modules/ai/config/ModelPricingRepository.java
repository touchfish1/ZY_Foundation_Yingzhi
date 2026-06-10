package com.zhangyuan.modules.ai.config;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ModelPricingRepository extends JpaRepository<ModelPricingConfig, Long> {
    Optional<ModelPricingConfig> findByModelName(String modelName);
}

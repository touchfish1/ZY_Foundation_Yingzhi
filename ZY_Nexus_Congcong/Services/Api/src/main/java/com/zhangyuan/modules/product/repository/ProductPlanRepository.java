package com.zhangyuan.modules.product.repository;

import com.zhangyuan.modules.product.domain.ProductPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductPlanRepository extends JpaRepository<ProductPlan, Long> {

    Optional<ProductPlan> findByCode(String code);
    boolean existsByCode(String code);
    List<ProductPlan> findByGroupIdOrderBySortOrderAsc(Long groupId);
}

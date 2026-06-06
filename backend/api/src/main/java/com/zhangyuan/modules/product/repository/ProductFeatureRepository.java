package com.zhangyuan.modules.product.repository;

import com.zhangyuan.modules.product.adapter.out.persistence.ProductFeature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductFeatureRepository extends JpaRepository<ProductFeature, Long> {

    List<ProductFeature> findByPlanIdOrderBySortOrderAsc(Long planId);

    List<ProductFeature> findByPlanId(Long planId);

    void deleteByPlanId(Long planId);
}

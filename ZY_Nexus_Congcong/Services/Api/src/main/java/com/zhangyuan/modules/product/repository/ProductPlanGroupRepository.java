package com.zhangyuan.modules.product.repository;

import com.zhangyuan.modules.product.domain.ProductPlanGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductPlanGroupRepository extends JpaRepository<ProductPlanGroup, Long> {

    Optional<ProductPlanGroup> findByCode(String code);
    boolean existsByCode(String code);
    List<ProductPlanGroup> findAllByOrderBySortOrderAsc();
}

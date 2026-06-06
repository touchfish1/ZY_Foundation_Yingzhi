package com.zhangyuan.modules.product.repository;

import com.zhangyuan.modules.product.adapter.out.persistence.ProductPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductPlanRepository extends JpaRepository<ProductPlan, Long> {

    Optional<ProductPlan> findByCode(String code);
    boolean existsByCode(String code);
    List<ProductPlan> findByGroupIdOrderBySortOrderAsc(Long groupId);
    List<ProductPlan> findAllByOrderBySortOrderAsc();

    Page<ProductPlan> findAllByOrderBySortOrderAsc(Pageable pageable);

    void deleteByGroupId(Long groupId);
}

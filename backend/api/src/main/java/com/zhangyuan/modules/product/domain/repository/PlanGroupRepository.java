package com.zhangyuan.modules.product.domain.repository;

import com.zhangyuan.modules.product.domain.model.Plan;
import com.zhangyuan.modules.product.domain.model.PlanGroup;
import java.util.List;
import java.util.Optional;

public interface PlanGroupRepository {

    Optional<PlanGroup> findById(Long id);

    Optional<PlanGroup> findByCode(String code);

    Optional<Plan> findPlanByCode(String code);

    List<PlanGroup> findAllOrdered();

    PlanGroup save(PlanGroup group);

    void deleteById(Long id);

    boolean existsByCode(String code);
}

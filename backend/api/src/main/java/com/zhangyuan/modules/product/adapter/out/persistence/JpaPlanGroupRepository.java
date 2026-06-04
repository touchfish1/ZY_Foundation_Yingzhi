package com.zhangyuan.modules.product.adapter.out.persistence;

import com.zhangyuan.modules.product.domain.model.PlanGroup;
import com.zhangyuan.modules.product.domain.repository.PlanGroupRepository;
import com.zhangyuan.modules.product.repository.ProductPlanGroupRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaPlanGroupRepository implements PlanGroupRepository {

    private final ProductPlanGroupRepository jpaRepository;

    public JpaPlanGroupRepository(ProductPlanGroupRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<PlanGroup> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<PlanGroup> findByCode(String code) {
        return jpaRepository.findByCode(code).map(this::toDomain);
    }

    @Override
    public List<PlanGroup> findAllOrdered() {
        return jpaRepository.findAllByOrderBySortOrderAsc().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public PlanGroup save(PlanGroup group) {
        ProductPlanGroup entity = new ProductPlanGroup(
                group.getCode(), group.getName(), group.getDescription(), group.getSortOrder());
        entity = jpaRepository.save(entity);
        return toDomain(entity);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.findByCode(code).isPresent();
    }

    private PlanGroup toDomain(ProductPlanGroup entity) {
        PlanGroup group = new PlanGroup(entity.getCode(), entity.getName(), entity.getDescription(), entity.getSortOrder());
        return group;
    }
}

package com.zhangyuan.modules.product.adapter.out.persistence;

import com.zhangyuan.modules.product.domain.model.PlanGroup;
import com.zhangyuan.modules.product.repository.ProductFeatureRepository;
import com.zhangyuan.modules.product.repository.ProductPlanGroupRepository;
import com.zhangyuan.modules.product.repository.ProductPlanRepository;
import com.zhangyuan.modules.product.repository.ProductPriceRepository;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JpaPlanGroupRepositoryTest {

    private final ProductPlanGroupRepository jpaRepository = mock(ProductPlanGroupRepository.class);
    private final ProductPlanRepository planRepository = mock(ProductPlanRepository.class);
    private final ProductPriceRepository priceRepository = mock(ProductPriceRepository.class);
    private final ProductFeatureRepository featureRepository = mock(ProductFeatureRepository.class);
    private final JpaPlanGroupRepository repository = new JpaPlanGroupRepository(
            jpaRepository, planRepository, priceRepository, featureRepository);

    @Test
    void findByCodeDelegates() {
        ProductPlanGroup entity = new ProductPlanGroup(
                "api_plans", "API Plans", "Desc", 10);
        entity = setEntityId(entity, 1L);
        when(jpaRepository.findByCode("api_plans")).thenReturn(Optional.of(entity));
        when(planRepository.findByGroupIdOrderBySortOrderAsc(1L)).thenReturn(Collections.emptyList());

        Optional<PlanGroup> result = repository.findByCode("api_plans");

        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("api_plans");
    }

    @Test
    void saveNewGroup() {
        PlanGroup group = new PlanGroup("new", "New", "Desc", 5);
        ProductPlanGroup savedEntity = new ProductPlanGroup(
                "new", "New", "Desc", 5);
        savedEntity = setEntityId(savedEntity, 1L);
        when(jpaRepository.save(any())).thenReturn(savedEntity);
        when(planRepository.findByGroupIdOrderBySortOrderAsc(1L)).thenReturn(Collections.emptyList());

        PlanGroup result = repository.save(group);

        assertThat(result.getCode()).isEqualTo("new");
        verify(jpaRepository).save(any());
    }

    private ProductPlanGroup setEntityId(ProductPlanGroup entity, Long id) {
        try {
            java.lang.reflect.Field idField = ProductPlanGroup.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
            return entity;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

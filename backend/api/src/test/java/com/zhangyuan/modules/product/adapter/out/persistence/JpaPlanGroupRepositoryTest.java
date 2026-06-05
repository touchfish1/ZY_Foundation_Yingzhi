package com.zhangyuan.modules.product.adapter.out.persistence;

import com.zhangyuan.modules.product.adapter.out.persistence.ProductPlanGroup;
import com.zhangyuan.modules.product.domain.model.PlanGroup;
import com.zhangyuan.modules.product.repository.ProductPlanGroupRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JpaPlanGroupRepositoryTest {

    private final ProductPlanGroupRepository jpaRepository = mock(ProductPlanGroupRepository.class);
    private final JpaPlanGroupRepository repository = new JpaPlanGroupRepository(jpaRepository);

    @Test
    void findByCodeDelegates() {
        ProductPlanGroup entity = new ProductPlanGroup(
                "api_plans", "API Plans", "Desc", 10);
        when(jpaRepository.findByCode("api_plans")).thenReturn(Optional.of(entity));

        Optional<PlanGroup> result = repository.findByCode("api_plans");

        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("api_plans");
    }

    @Test
    void saveNewGroup() {
        PlanGroup group = new PlanGroup("new", "New", "Desc", 5);
        ProductPlanGroup savedEntity = new ProductPlanGroup(
                "new", "New", "Desc", 5);
        when(jpaRepository.save(any())).thenReturn(savedEntity);

        PlanGroup result = repository.save(group);

        assertThat(result.getCode()).isEqualTo("new");
        verify(jpaRepository).save(any());
    }
}

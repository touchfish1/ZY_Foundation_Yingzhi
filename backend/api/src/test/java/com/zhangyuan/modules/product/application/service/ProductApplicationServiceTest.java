package com.zhangyuan.modules.product.application.service;

import com.zhangyuan.modules.product.domain.model.PlanGroup;
import com.zhangyuan.modules.product.domain.repository.PlanGroupRepository;
import com.zhangyuan.modules.product.domain.service.ProductDomainService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductApplicationServiceTest {

    private final PlanGroupRepository planGroupRepository = mock(PlanGroupRepository.class);
    private final ProductDomainService productDomainService = new ProductDomainService();
    private final ProductApplicationService service = new ProductApplicationService(planGroupRepository, productDomainService);

    @Test
    void createGroupSavesAndReturns() {
        when(planGroupRepository.existsByCode("new_group")).thenReturn(false);
        when(planGroupRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PlanGroup result = service.createGroup("new_group", "New Group", "Desc", 5);

        assertThat(result.getCode()).isEqualTo("new_group");
        assertThat(result.getName()).isEqualTo("New Group");
        verify(planGroupRepository).save(any());
    }

    @Test
    void findByCodeDelegates() {
        service.findByCode("api_plans");

        verify(planGroupRepository).findByCode("api_plans");
    }

    @Test
    void listAllDelegates() {
        service.listAll();

        verify(planGroupRepository).findAllOrdered();
    }
}

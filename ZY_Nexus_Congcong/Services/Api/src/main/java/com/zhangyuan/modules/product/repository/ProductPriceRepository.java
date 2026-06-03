package com.zhangyuan.modules.product.repository;

import com.zhangyuan.modules.product.domain.ProductPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductPriceRepository extends JpaRepository<ProductPrice, Long> {

    List<ProductPrice> findByPlanId(Long planId);

    Optional<ProductPrice> findFirstByPlanIdAndBillingCycleAndCurrencyAndStatus(Long planId, String billingCycle, String currency, String status);
}

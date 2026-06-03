package com.zhangyuan.modules.product.repository;

import com.zhangyuan.modules.product.domain.ProductPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductPriceRepository extends JpaRepository<ProductPrice, Long> {

    List<ProductPrice> findByPlanId(Long planId);
}

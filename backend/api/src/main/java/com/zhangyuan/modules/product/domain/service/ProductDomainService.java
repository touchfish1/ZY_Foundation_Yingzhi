package com.zhangyuan.modules.product.domain.service;

import com.zhangyuan.modules.product.domain.model.PlanGroup;
import com.zhangyuan.modules.product.domain.repository.PlanGroupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 产品领域服务，包含方案组创建的核心业务规则。
 */
@Service
public class ProductDomainService {

    private static final Logger log = LoggerFactory.getLogger(ProductDomainService.class);

    /**
     * 验证方案组编码是否已存在。
     *
     * @param repository 方案组仓库
     * @param code       方案组编码
     * @throws IllegalArgumentException 编码已存在时抛出
     */
    public void validateGroupCreation(PlanGroupRepository repository, String code) {
        if (repository.existsByCode(code)) {
            log.warn("Plan group code already exists: {}", code);
            throw new IllegalArgumentException("Plan group code already exists: " + code);
        }
    }
}

package com.zhangyuan.modules.product.application.service;

import com.zhangyuan.modules.product.domain.model.PlanGroup;
import com.zhangyuan.modules.product.domain.repository.PlanGroupRepository;
import com.zhangyuan.modules.product.domain.service.ProductDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 产品应用服务，负责方案组创建和管理编排。
 */
@Service
public class ProductApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ProductApplicationService.class);

    private final PlanGroupRepository planGroupRepository;
    private final ProductDomainService productDomainService;

    public ProductApplicationService(PlanGroupRepository planGroupRepository, ProductDomainService productDomainService) {
        this.planGroupRepository = planGroupRepository;
        this.productDomainService = productDomainService;
    }

    /**
     * 创建方案组。
     *
     * @param code        方案组编码
     * @param name        方案组名称
     * @param description 方案组描述
     * @param sortOrder   排序值
     * @return 创建后的方案组
     */
    @Transactional
    public PlanGroup createGroup(String code, String name, String description, int sortOrder) {
        log.info("Creating plan group: code={}, name={}", code, name);
        productDomainService.validateGroupCreation(planGroupRepository, code);
        PlanGroup group = new PlanGroup(code, name, description, sortOrder);
        PlanGroup saved = planGroupRepository.save(group);
        log.info("Plan group created: id={}, code={}", saved.getId(), saved.getCode());
        return saved;
    }

    /**
     * 根据编码查询方案组。
     *
     * @param code 方案组编码
     * @return 方案组 Optional
     */
    @Transactional(readOnly = true)
    public Optional<PlanGroup> findByCode(String code) {
        return planGroupRepository.findByCode(code);
    }

    /**
     * 获取所有方案组列表。
     *
     * @return 方案组列表
     */
    @Transactional(readOnly = true)
    public List<PlanGroup> listAll() {
        return planGroupRepository.findAllOrdered();
    }

    /**
     * 禁用指定方案组。
     *
     * @param id 方案组 ID
     */
    @Transactional
    public void disableGroup(Long id) {
        log.info("Disabling plan group: {}", id);
        PlanGroup group = planGroupRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Plan group not found: {}", id);
                    return new IllegalArgumentException("Plan group not found: " + id);
                });
        group.disable();
        planGroupRepository.save(group);
        log.info("Plan group disabled: {}", id);
    }
}

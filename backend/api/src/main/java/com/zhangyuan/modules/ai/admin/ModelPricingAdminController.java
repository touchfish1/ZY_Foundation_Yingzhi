package com.zhangyuan.modules.ai.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.ai.config.ModelPricingConfig;
import com.zhangyuan.modules.ai.config.ModelPricingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/ai/pricing")
@SaCheckPermission("ai:pricing:list")
public class ModelPricingAdminController {

    private static final Logger log = LoggerFactory.getLogger(ModelPricingAdminController.class);

    private final ModelPricingRepository modelPricingRepository;

    public ModelPricingAdminController(ModelPricingRepository modelPricingRepository) {
        this.modelPricingRepository = modelPricingRepository;
    }

    @GetMapping
    public ApiResponse<List<ModelPricingConfig>> list() {
        log.info("Listing all model pricings");
        return ApiResponse.ok(modelPricingRepository.findAll());
    }

    @PostMapping
    @SaCheckPermission("ai:pricing:create")
    public ApiResponse<ModelPricingConfig> create(@RequestBody ModelPricingConfig config) {
        log.info("Creating model pricing: modelName={}", config.getModelName());
        return ApiResponse.ok(modelPricingRepository.save(config));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("ai:pricing:create")
    public ApiResponse<ModelPricingConfig> update(@PathVariable Long id, @RequestBody ModelPricingConfig config) {
        log.info("Updating model pricing: id={}", id);
        ModelPricingConfig existing = modelPricingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Model pricing not found: " + id));
        existing.setModelName(config.getModelName());
        existing.setInputPrice(config.getInputPrice());
        existing.setOutputPrice(config.getOutputPrice());
        existing.setCurrency(config.getCurrency());
        existing.setEffectiveFrom(config.getEffectiveFrom());
        existing.setEffectiveTo(config.getEffectiveTo());
        return ApiResponse.ok(modelPricingRepository.save(existing));
    }
}

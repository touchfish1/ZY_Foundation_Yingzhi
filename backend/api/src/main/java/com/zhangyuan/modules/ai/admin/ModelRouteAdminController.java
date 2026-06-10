package com.zhangyuan.modules.ai.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.ai.config.ModelRouteConfig;
import com.zhangyuan.modules.ai.config.ModelRouteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/ai/models")
@SaCheckPermission("ai:model:list")
public class ModelRouteAdminController {

    private static final Logger log = LoggerFactory.getLogger(ModelRouteAdminController.class);

    private final ModelRouteRepository modelRouteRepository;

    public ModelRouteAdminController(ModelRouteRepository modelRouteRepository) {
        this.modelRouteRepository = modelRouteRepository;
    }

    @GetMapping
    public ApiResponse<List<ModelRouteConfig>> list() {
        log.info("Listing all model routes");
        return ApiResponse.ok(modelRouteRepository.findAll());
    }

    @PostMapping
    @SaCheckPermission("ai:model:create")
    public ApiResponse<ModelRouteConfig> create(@RequestBody ModelRouteConfig config) {
        log.info("Creating model route: modelName={}, provider={}", config.getModelName(), config.getProvider());
        return ApiResponse.ok(modelRouteRepository.save(config));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("ai:model:update")
    public ApiResponse<ModelRouteConfig> update(@PathVariable Long id, @RequestBody ModelRouteConfig config) {
        log.info("Updating model route: id={}", id);
        ModelRouteConfig existing = modelRouteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Model route not found: " + id));
        existing.setModelName(config.getModelName());
        existing.setProvider(config.getProvider());
        existing.setProviderModelName(config.getProviderModelName());
        existing.setModelType(config.getModelType());
        existing.setStatus(config.getStatus());
        return ApiResponse.ok(modelRouteRepository.save(existing));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("ai:model:delete")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        log.info("Deleting model route: id={}", id);
        modelRouteRepository.deleteById(id);
        return ApiResponse.ok(null);
    }
}

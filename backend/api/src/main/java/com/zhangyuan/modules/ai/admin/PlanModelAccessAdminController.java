package com.zhangyuan.modules.ai.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.ai.config.PlanModelAccess;
import com.zhangyuan.modules.ai.config.PlanModelAccessRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/ai/plan-access")
@SaCheckPermission("ai:access:list")
public class PlanModelAccessAdminController {

    private static final Logger log = LoggerFactory.getLogger(PlanModelAccessAdminController.class);

    private final PlanModelAccessRepository planModelAccessRepository;

    public PlanModelAccessAdminController(PlanModelAccessRepository planModelAccessRepository) {
        this.planModelAccessRepository = planModelAccessRepository;
    }

    @GetMapping
    public ApiResponse<List<PlanModelAccess>> list() {
        log.info("Listing all plan-model access rules");
        return ApiResponse.ok(planModelAccessRepository.findAll());
    }

    @PostMapping
    @SaCheckPermission("ai:access:create")
    public ApiResponse<PlanModelAccess> create(@RequestBody PlanModelAccess access) {
        log.info("Creating plan-model access: planCode={}, modelName={}", access.getPlanCode(), access.getModelName());
        return ApiResponse.ok(planModelAccessRepository.save(access));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("ai:access:delete")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        log.info("Deleting plan-model access: id={}", id);
        planModelAccessRepository.deleteById(id);
        return ApiResponse.ok(null);
    }
}

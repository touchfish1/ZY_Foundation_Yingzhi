package com.zhangyuan.ai.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

/**
 * Checks whether a user's plan allows access to a requested model.
 * <p>
 * Uses hardcoded plan→model mappings for now (ai-service lacks DB access to
 * the api module's {@code ai_plan_model_access} table). This will be replaced
 * with Feign client calls or shared DB access in the future.
 */
@Service
public class ModelAccessService {

    private static final Logger log = LoggerFactory.getLogger(ModelAccessService.class);

    private static final Map<String, Set<String>> PLAN_MODEL_MAP = Map.of(
            "free", Set.of("gpt-4o-mini", "gpt-3.5-turbo"),
            "pro", Set.of("gpt-4o", "gpt-4-turbo", "gpt-4o-mini", "gpt-3.5-turbo", "claude-3-haiku"),
            "enterprise", Set.of("gpt-4o", "gpt-4-turbo", "gpt-4o-mini", "gpt-3.5-turbo",
                    "claude-3-opus", "claude-3-sonnet", "claude-3-haiku")
    );

    private static final Set<String> DEFAULT_ALLOWED = Set.of("gpt-4o-mini", "gpt-3.5-turbo");

    /**
     * Checks whether the given plan allows access to the specified model.
     *
     * @param planCode  the user's plan code (e.g. "free", "pro", "enterprise"), may be null or unknown
     * @param modelName the requested model identifier (e.g. "gpt-4o")
     * @throws AccessDeniedException if the plan does not include the model
     */
    public void checkAccess(String planCode, String modelName) {
        Set<String> allowedModels = (planCode != null && PLAN_MODEL_MAP.containsKey(planCode))
                ? PLAN_MODEL_MAP.get(planCode)
                : DEFAULT_ALLOWED;

        if (allowedModels.contains(modelName)) {
            if (log.isDebugEnabled()) {
                log.debug("Access GRANTED: planCode={}, model={}", planCode, modelName);
            }
            return;
        }

        log.warn("Access DENIED: planCode={}, model={}", planCode, modelName);
        throw new AccessDeniedException(
                "Your current plan does not include access to model: " + modelName
                        + ". Allowed models for plan '" + (planCode != null ? planCode : "default") + "': "
                        + String.join(", ", allowedModels)
        );
    }
}

package com.zhangyuan.ai.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ModelAccessServiceTest {

    private ModelAccessService modelAccessService;

    @BeforeEach
    void setUp() {
        modelAccessService = new ModelAccessService();
    }

    @Test
    void checkAccess_freePlan_allowedModel_doesNotThrow() {
        // free plan: gpt-4o-mini, gpt-3.5-turbo
        assertThatCode(() -> modelAccessService.checkAccess("free", "gpt-4o-mini"))
                .doesNotThrowAnyException();
    }

    @Test
    void checkAccess_freePlan_deniedModel_throwsAccessDenied() {
        // free plan cannot access gpt-4o
        assertThatThrownBy(() -> modelAccessService.checkAccess("free", "gpt-4o"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("gpt-4o")
                .hasMessageContaining("free");
    }

    @Test
    void checkAccess_proPlan_allowedModel_doesNotThrow() {
        // pro plan: gpt-4o, gpt-4-turbo, gpt-4o-mini, gpt-3.5-turbo, claude-3-haiku
        assertThatCode(() -> modelAccessService.checkAccess("pro", "gpt-4o"))
                .doesNotThrowAnyException();

        assertThatCode(() -> modelAccessService.checkAccess("pro", "claude-3-haiku"))
                .doesNotThrowAnyException();
    }

    @Test
    void checkAccess_proPlan_deniedModel_throwsAccessDenied() {
        // pro plan cannot access claude-3-opus
        assertThatThrownBy(() -> modelAccessService.checkAccess("pro", "claude-3-opus"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("claude-3-opus");
    }

    @Test
    void checkAccess_enterprisePlan_allModelsAllowed() {
        // enterprise plan: all models
        assertThatCode(() -> modelAccessService.checkAccess("enterprise", "claude-3-opus"))
                .doesNotThrowAnyException();

        assertThatCode(() -> modelAccessService.checkAccess("enterprise", "claude-3-sonnet"))
                .doesNotThrowAnyException();

        assertThatCode(() -> modelAccessService.checkAccess("enterprise", "gpt-4o"))
                .doesNotThrowAnyException();
    }

    @Test
    void checkAccess_nullPlan_usesDefaultAllowed() {
        // null plan defaults to free: gpt-4o-mini, gpt-3.5-turbo
        assertThatCode(() -> modelAccessService.checkAccess(null, "gpt-4o-mini"))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> modelAccessService.checkAccess(null, "gpt-4o"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("default");
    }

    @Test
    void checkAccess_unknownPlan_usesDefaultAllowed() {
        // unknown plan defaults to free
        assertThatCode(() -> modelAccessService.checkAccess("unknown-plan", "gpt-3.5-turbo"))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> modelAccessService.checkAccess("unknown-plan", "claude-3-opus"))
                .isInstanceOf(AccessDeniedException.class);
    }
}

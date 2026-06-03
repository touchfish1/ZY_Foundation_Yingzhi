package com.zhangyuan.architecture;

import com.zhangyuan.ZhangyuanApplication;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModularityTest {

    @Test
    void verifyModularStructure() {
        ApplicationModules modules = ApplicationModules.of(ZhangyuanApplication.class);
        modules.forEach(System.out::println);
        modules.verify();
    }

    @Test
    void createModuleDocumentation() {
        ApplicationModules modules = ApplicationModules.of(ZhangyuanApplication.class);
        new Documenter(modules)
                .writeDocumentation()
                .writeIndividualModuleDocuments();
    }
}

package com.zhangyuan.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.zhangyuan")
class DddLayerTest {

    @ArchTest
    static final ArchRule domainShouldNotDependOnInfrastructure =
            noClasses()
                    .that().resideInAnyPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..adapter..", "..infrastructure..", "..application..")
                    .because("Domain layer must not depend on infrastructure or application layers");

    @ArchTest
    static final ArchRule applicationShouldNotDependOnAdapters =
            noClasses()
                    .that().resideInAnyPackage("..application..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..adapter..")
                    .because("Application layer must not depend on adapter layer");

    @ArchTest
    static final ArchRule domainModelsShouldOnlyUseDddKernel =
            classes()
                    .that().resideInAnyPackage("..domain.model..")
                    .should().onlyDependOnClassesThat()
                    .resideInAnyPackage(
                            "..domain.model..",
                            "..common.ddd..",
                            "java..",
                            "org.."
                    )
                    .because("Domain models should only depend on DDD kernel and JDK");

    @ArchTest
    static final ArchRule repositoriesShouldBeInterfacesInDomain =
            classes()
                    .that().resideInAnyPackage("..domain.repository..")
                    .should().beInterfaces()
                    .andShould().haveSimpleNameEndingWith("Repository")
                    .because("Domain repository contracts must be interfaces");

    @ArchTest
    static final ArchRule adaptersShouldImplementDomainPorts =
            classes()
                    .that().resideInAnyPackage("..adapter.out.persistence..")
                    .should().implement(
                            JavaClasses.Predicates.that()
                                    .and(classes -> classes.getInterfaces().stream()
                                            .anyMatch(i -> i.getName().endsWith("Repository")))
                    )
                    .because("Persistence adapters must implement domain repository interfaces");
}

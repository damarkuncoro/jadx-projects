package dexforge.core.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * SOLID Enforcement: Ensures DexForge Core remains clean and independent.
 */
@AnalyzeClasses(packages = "dexforge.core", importOptions = ImportOption.DoNotIncludeTests.class)
public class CleanArchitectureTest {

    @ArchTest
    static final ArchRule core_should_not_depend_on_jadx_internals =
        noClasses().that().resideInAPackage("dexforge.core..")
            .should().dependOnClassesThat().resideInAnyPackage("jadx.core..", "jadx.gui..");

    @ArchTest
    static final ArchRule core_should_be_independent_of_gui =
        noClasses().that().resideInAPackage("dexforge.core..")
            .should().dependOnClassesThat().resideInAPackage("dexforge.gui..");

    @ArchTest
    static final ArchRule core_should_not_use_java_14_plus_features =
        noClasses().should().dependOnClassesThat().resideInAnyPackage("java.util.record..", "java.lang.runtime..");
}

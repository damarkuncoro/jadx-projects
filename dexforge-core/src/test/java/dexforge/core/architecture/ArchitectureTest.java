package dexforge.core.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "dexforge.core", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

	@ArchTest
	public static final ArchRule coreShouldNotDependOnJadx =
			noClasses().that().resideInAPackage("dexforge.core..")
					.should().dependOnClassesThat().resideInAPackage("jadx..");

	@ArchTest
	public static final ArchRule coreShouldOnlyDependOnApiAndStandardLibs =
			noClasses().that().resideInAPackage("dexforge.core..")
					.should().dependOnClassesThat().resideInAPackage("dexforge.engine.jadx..");
}

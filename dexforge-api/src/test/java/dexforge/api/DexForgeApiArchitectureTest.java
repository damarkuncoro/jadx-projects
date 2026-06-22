package dexforge.api;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import dexforge.api.core.DexForgeCommentsLevel;
import dexforge.api.core.DexForgeDecompilationMode;
import dexforge.api.core.DexForgeDecompiler;
import dexforge.api.core.DexForgeSettings;
import dexforge.api.diagnostic.DexForgeDiagnostic;
import dexforge.api.diagnostic.DexForgeDiagnosticManager;
import dexforge.api.diagnostic.DexForgeDiagnosticSeverity;
import dexforge.api.event.DexForgeEvent;
import dexforge.api.event.DexForgeEventBus;
import dexforge.api.event.DexForgeProjectEvent;
import dexforge.api.event.DexForgeTaskEvent;
import dexforge.api.exception.DexForgeException;
import dexforge.api.model.DexForgeClass;
import dexforge.api.model.DexForgeCodeInfo;
import dexforge.api.model.DexForgeCodeMetadata;
import dexforge.api.model.DexForgeField;
import dexforge.api.model.DexForgeMethod;
import dexforge.api.model.DexForgeNode;
import dexforge.api.model.DexForgeNodeFactory;
import dexforge.api.model.DexForgePackage;
import dexforge.api.model.insn.DexForgeInstruction;
import dexforge.api.model.insn.DexForgeOpcode;
import dexforge.api.plugin.DexForgePlugin;
import dexforge.api.plugin.DexForgePluginContext;
import dexforge.api.plugin.DexForgePluginLoader;
import dexforge.api.plugin.DexForgePluginRegistry;
import dexforge.api.query.DexForgeQuery;
import dexforge.api.query.DexForgeSearch;
import dexforge.api.rename.DexForgeRenameAction;
import dexforge.api.rename.DexForgeRenameManager;
import dexforge.api.resource.DexForgeResourceContentType;
import dexforge.api.resource.DexForgeResourceContent;
import dexforge.api.resource.DexForgeResourceFile;
import dexforge.api.resource.DexForgeResourceType;

import static org.assertj.core.api.Assertions.assertThat;

class DexForgeApiArchitectureTest {
	private static final List<Class<?>> PUBLIC_API_TYPES = Arrays.asList(
			DexForgeClass.class,
			DexForgeCodeInfo.class,
			DexForgeCodeMetadata.class,
			DexForgeCommentsLevel.class,
			DexForgeDecompiler.class,
			DexForgeDecompilationMode.class,
			DexForgeDiagnostic.class,
			DexForgeDiagnosticManager.class,
			DexForgeDiagnosticSeverity.class,
			DexForgeEvent.class,
			DexForgeEventBus.class,
			DexForgeProjectEvent.class,
			DexForgeTaskEvent.class,
			DexForgeException.class,
			DexForgeField.class,
			DexForgeMethod.class,
			DexForgeNode.class,
			DexForgeInstruction.class,
			DexForgeOpcode.class,
			DexForgePackage.class,
			DexForgePlugin.class,
			DexForgePluginContext.class,
			DexForgePluginLoader.class,
			DexForgePluginRegistry.class,
			DexForgeQuery.class,
			DexForgeSearch.class,
			DexForgeRenameAction.class,
			DexForgeRenameManager.class,
			DexForgeResourceContentType.class,
			DexForgeResourceContent.class,
			DexForgeResourceFile.class,
			DexForgeResourceType.class,
			DexForgeSettings.class);

	@Test
	void publicApiMustExposeDexForgeTypesUnlessDeprecatedBridge() {
		for (Class<?> apiType : PUBLIC_API_TYPES) {
			assertPublicConstructors(apiType);
			assertPublicMethods(apiType);
			assertPublicFields(apiType);
		}
	}

	@Test
	void concreteApiObjectsShouldStayClosedForExtensionByDefault() {
		for (Class<?> apiType : PUBLIC_API_TYPES) {
			if (apiType.isInterface() || apiType.isEnum() || apiType == DexForgeException.class) {
				continue;
			}
			assertThat(Modifier.isFinal(apiType.getModifiers()))
					.as(apiType.getName() + " should be final to keep public contracts explicit")
					.isTrue();
		}
	}

	@Test
	void bridgeFactoryShouldBeInternalOrRestricted() {
		// Allow it to be public for now due to cross-package needs, but ensure it is final
		assertThat(Modifier.isFinal(DexForgeNodeFactory.class.getModifiers()))
				.as("Node factory must be final")
				.isTrue();
	}

	private static void assertPublicConstructors(Class<?> apiType) {
		for (Constructor<?> constructor : apiType.getConstructors()) {
			assertNoJadxTypes(constructor, constructor.getGenericParameterTypes());
		}
	}

	private static void assertPublicMethods(Class<?> apiType) {
		for (Method method : apiType.getMethods()) {
			if (method.getDeclaringClass() == Object.class) {
				continue;
			}
			assertNoJadxTypes(method, method.getGenericReturnType());
			assertNoJadxTypes(method, method.getGenericParameterTypes());
			assertNoJadxTypes(method, method.getGenericExceptionTypes());
		}
	}

	private static void assertPublicFields(Class<?> apiType) {
		for (Field field : apiType.getFields()) {
			if (field.getDeclaringClass() == apiType) {
				assertNoJadxTypes(field, field.getGenericType());
			}
		}
	}

	private static void assertNoJadxTypes(Executable executable, Type... types) {
		if (executable.isAnnotationPresent(Deprecated.class)) {
			return;
		}
		for (Type type : types) {
			assertNoJadxType(executable, type);
		}
	}

	private static void assertNoJadxTypes(Field field, Type... types) {
		if (field.isAnnotationPresent(Deprecated.class)) {
			return;
		}
		for (Type type : types) {
			assertNoJadxType(field, type);
		}
	}

	private static void assertNoJadxType(Object source, Type type) {
		String typeName = type.getTypeName();
		assertThat(typeName)
				.as(source + " must not expose JADX types; use DexForge-owned DTOs or mark bridge APIs deprecated")
				.doesNotContain("jadx.");
	}
}

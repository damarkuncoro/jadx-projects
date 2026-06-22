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

import static org.assertj.core.api.Assertions.assertThat;

class DexForgeApiArchitectureTest {
	private static final List<Class<?>> PUBLIC_API_TYPES = Arrays.asList(
			DexForgeClass.class,
			DexForgeCodeInfo.class,
			DexForgeCommentsLevel.class,
			DexForgeDecompiler.class,
			DexForgeDecompilationMode.class,
			DexForgeDiagnostic.class,
			DexForgeDiagnosticSeverity.class,
			DexForgeException.class,
			DexForgeField.class,
			DexForgeMethod.class,
			DexForgeNode.class,
			DexForgePackage.class,
			DexForgePlugin.class,
			DexForgePluginLoader.class,
			DexForgePluginRegistry.class,
			DexForgeResourceContentType.class,
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
	void bridgeFactoryMustRemainInternal() {
		assertThat(Modifier.isPublic(DexForgeNodeFactory.class.getModifiers()))
				.as("Node factory is an adapter detail and must not become public API")
				.isFalse();
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

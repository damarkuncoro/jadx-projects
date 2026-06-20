package dexforge.core.infrastructure.jadx;

import dexforge.domain.model.source.MethodSignature;

import jadx.api.JavaClass;
import jadx.api.JavaMethod;

public final class JadxMethodSignatureMapper {
	private JadxMethodSignatureMapper() {
	}

	public static MethodSignature fromJavaMethod(JavaClass javaClass, JavaMethod method) {
		return MethodSignature.of(javaClass.getFullName(), method.getName(), method.toString());
	}
}

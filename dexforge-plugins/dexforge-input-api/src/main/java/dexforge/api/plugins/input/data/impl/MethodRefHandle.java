package dexforge.api.plugins.input.data.impl;

import org.jetbrains.annotations.Nullable;

import dexforge.api.plugins.input.data.IFieldData;
import dexforge.api.plugins.input.data.IMethodHandle;
import dexforge.api.plugins.input.data.IMethodRef;
import dexforge.api.plugins.input.data.MethodHandleType;

public class MethodRefHandle implements IMethodHandle {

	private final MethodHandleType type;
	private final IMethodRef methodRef;

	public MethodRefHandle(MethodHandleType type, IMethodRef methodRef) {
		this.type = type;
		this.methodRef = methodRef;
	}

	@Override
	public MethodHandleType getType() {
		return type;
	}

	@Override
	public @Nullable IMethodRef getMethodRef() {
		return methodRef;
	}

	@Override
	public @Nullable IFieldData getFieldRef() {
		return null;
	}

	@Override
	public void load() {
		methodRef.load();
	}

	@Override
	public String toString() {
		return type + ": " + methodRef;
	}
}

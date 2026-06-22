package dexforge.core.infrastructure.jadx;

import java.util.List;

import jadx.core.dex.info.FieldInfo;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.FieldNode;
import jadx.core.dex.nodes.MethodNode;

/**
 * Internal helper to bridge JADX Node operations without exposing them to public API.
 */
public final class JadxNodeHelper {
	private JadxNodeHelper() {
	}

	// ClassNode operations
	public static String getClassName(Object node) {
		return ((ClassNode) node).getShortName();
	}

	public static String getClassFullName(Object node) {
		return ((ClassNode) node).getFullName();
	}

	public static String getClassPackage(Object node) {
		return ((ClassNode) node).getPackage();
	}

	public static String getClassRawName(Object node) {
		return ((ClassNode) node).getRawName();
	}

	public static String getClassSmali(Object node) {
		return ((ClassNode) node).getDisassembledCode();
	}

	public static boolean isClassNoCode(Object node) {
		return ((ClassNode) node).contains(jadx.core.dex.attributes.AFlag.DONT_GENERATE);
	}

	public static boolean isClassInner(Object node) {
		return ((ClassNode) node).isInner();
	}

	public static List<?> getInnerClasses(Object node) {
		return ((ClassNode) node).getInnerClasses();
	}

	public static List<?> getInlinedClasses(Object node) {
		return ((ClassNode) node).getInlinedClasses();
	}

	public static List<?> getFields(Object node) {
		return ((ClassNode) node).getFields();
	}

	public static List<?> getMethods(Object node) {
		return ((ClassNode) node).getMethods();
	}

	public static Object searchMethodByShortId(Object node, String shortId) {
		return ((ClassNode) node).searchMethodByShortId(shortId);
	}

	public static List<?> getDependencies(Object node) {
		return ((ClassNode) node).getDependencies();
	}

	public static int getTotalDepsCount(Object node) {
		return ((ClassNode) node).getTotalDepsCount();
	}

	public static Object getParentClass(Object node) {
		return ((ClassNode) node).getParentClass();
	}

	public static Object getTopParentClass(Object node) {
		return ((ClassNode) node).getTopParentClass();
	}

	public static int getDefPos(Object node) {
		return ((ClassNode) node).getDefPosition();
	}

	public static List<ClassNode> getUseIn(Object node) {
		return ((ClassNode) node).getUseIn();
	}

	public static List<?> getClassUseIn(Object node) {
		Object javaNode = getJavaNode(node);
		if (javaNode instanceof jadx.api.JavaClass) {
			return ((jadx.api.JavaClass) javaNode).getUseIn();
		}
		return java.util.Collections.emptyList();
	}

	public static boolean isClassDecompiled(Object node) {
		Object javaNode = getJavaNode(node);
		if (javaNode instanceof jadx.api.JavaClass) {
			return ((jadx.api.JavaClass) javaNode).getCodeInfo() != jadx.api.ICodeInfo.EMPTY;
		}
		return false;
	}

	public static void removeAlias(Object node) {
		if (node instanceof ClassNode) {
			((ClassNode) node).removeAlias();
		} else if (node instanceof MethodNode) {
			((MethodNode) node).getMethodInfo().removeAlias();
		} else if (node instanceof FieldNode) {
			((FieldNode) node).getFieldInfo().removeAlias();
		}
	}

	public static void rename(Object node, String newName) {
		if (node instanceof ClassNode) {
			((ClassNode) node).getClassInfo().changeShortName(newName);
		} else if (node instanceof MethodNode) {
			((MethodNode) node).getMethodInfo().setAlias(newName);
		} else if (node instanceof FieldNode) {
			((FieldNode) node).getFieldInfo().setAlias(newName);
		}
	}

	public static void unloadCode(Object node) {
		((ClassNode) node).unloadCode();
	}

	public static Object reloadCode(Object node) {
		return ((ClassNode) node).reloadCode();
	}

	public static Object decompile(Object node) {
		return ((ClassNode) node).decompile();
	}

	// MethodNode operations
	public static String getMethodAlias(Object node) {
		return ((MethodNode) node).getAlias();
	}

	public static String getMethodFullName(Object node) {
		return ((MethodNode) node).getMethodInfo().getFullName();
	}

	public static List<?> getMethodArgs(Object node) {
		return ((MethodNode) node).getMethodInfo().getArgumentsTypes();
	}

	public static String getMethodReturnType(Object node) {
		return ((MethodNode) node).getReturnType().toString();
	}

	public static boolean isConstructor(Object node) {
		return ((MethodNode) node).getMethodInfo().isConstructor();
	}

	public static boolean isClassInit(Object node) {
		return ((MethodNode) node).getMethodInfo().isClassInit();
	}

	public static String getMethodCode(Object node) {
		Object javaNode = getJavaNode(node);
		if (javaNode instanceof jadx.api.JavaMethod) {
			return ((jadx.api.JavaMethod) javaNode).getCodeStr();
		}
		return "";
	}

	public static boolean getMethodCallsSelf(Object node) {
		Object javaNode = getJavaNode(node);
		if (javaNode instanceof jadx.api.JavaMethod) {
			return ((jadx.api.JavaMethod) javaNode).callsSelf();
		}
		return false;
	}

	public static List<?> getMethodUsed(Object node) {
		Object javaNode = getJavaNode(node);
		if (javaNode instanceof jadx.api.JavaMethod) {
			return ((jadx.api.JavaMethod) javaNode).getUsed();
		}
		return java.util.Collections.emptyList();
	}

	public static List<?> getMethodOverrideRelated(Object node) {
		Object javaNode = getJavaNode(node);
		if (javaNode instanceof jadx.api.JavaMethod) {
			return ((jadx.api.JavaMethod) javaNode).getOverrideRelatedMethods();
		}
		return java.util.Collections.emptyList();
	}

	public static List<?> getMethodUseIn(Object node) {
		Object javaNode = getJavaNode(node);
		if (javaNode instanceof jadx.api.JavaMethod) {
			return ((jadx.api.JavaMethod) javaNode).getUseIn();
		}
		return java.util.Collections.emptyList();
	}

	public static List<?> getMethodInstructions(Object node) {
		MethodNode mth = (MethodNode) node;
		try {
			mth.load();
		} catch (Exception e) {
			// ignore or log
		}
		if (mth.getInstructions() == null) {
			return java.util.Collections.emptyList();
		}
		return java.util.Arrays.asList(mth.getInstructions());
	}

	public static String getInsnType(Object insn) {
		if (insn instanceof jadx.core.dex.instructions.InvokeNode) return "INVOKE";
		if (insn instanceof jadx.core.dex.instructions.IndexInsnNode) {
			jadx.core.dex.instructions.InsnType type = ((jadx.core.dex.nodes.InsnNode) insn).getType();
			if (type == jadx.core.dex.instructions.InsnType.CHECK_CAST) return "CAST";
			if (type == jadx.core.dex.instructions.InsnType.NEW_INSTANCE) return "NEW_INSTANCE";
			if (type == jadx.core.dex.instructions.InsnType.NEW_ARRAY) return "NEW_ARRAY";
			return "UNKNOWN";
		}
		if (insn instanceof jadx.core.dex.instructions.IfNode) return "IF";
		if (insn instanceof jadx.core.dex.instructions.GotoNode) return "GOTO";
		if (insn instanceof jadx.core.dex.instructions.SwitchInsn) return "SWITCH";
		if (insn instanceof jadx.core.dex.instructions.args.Named) return "CONST";
		return "UNKNOWN";
	}

	public static int getInsnOffset(Object insn) {
		return ((jadx.core.dex.nodes.InsnNode) insn).getOffset();
	}

	public static String getInsnMnemonic(Object insn) {
		return insn.toString();
	}

	public static Object getInsnReferencedNode(Object insn) {
		if (insn instanceof jadx.core.dex.instructions.InvokeNode) {
			return ((jadx.core.dex.instructions.InvokeNode) insn).getCallMth();
		}
		if (insn instanceof jadx.core.dex.instructions.IndexInsnNode) {
			return ((jadx.core.dex.instructions.IndexInsnNode) insn).getIndex();
		}
		return null;
	}

	public static List<String> getInsnOperands(Object insn) {
		jadx.core.dex.nodes.InsnNode node = (jadx.core.dex.nodes.InsnNode) insn;
		List<String> ops = new java.util.ArrayList<>();
		for (jadx.core.dex.instructions.args.InsnArg arg : node.getArguments()) {
			ops.add(arg.toString());
		}
		return ops;
	}

	public static boolean isMethodDecompiled(Object node) {
		// In JADX, methods don't have separate code info if they are part of a class decompile
		// but they can be unloaded.
		return !((MethodNode) node).isNoCode();
	}

	// FieldNode operations
	public static String getFieldAlias(Object node) {
		return ((FieldNode) node).getAlias();
	}

	public static String getFieldFullName(Object node) {
		return ((FieldInfo) ((FieldNode) node).getFieldInfo()).getFullId();
	}

	public static String getFieldRawName(Object node) {
		return ((FieldNode) node).getName();
	}

	public static String getFieldType(Object node) {
		return ((FieldNode) node).getType().toString();
	}

	public static List<?> getFieldUseIn(Object node) {
		Object javaNode = getJavaNode(node);
		if (javaNode instanceof jadx.api.JavaField) {
			return ((jadx.api.JavaField) javaNode).getUseIn();
		}
		return java.util.Collections.emptyList();
	}

	public static boolean isClassNode(Object node) {
		return node instanceof ClassNode;
	}

	public static boolean isMethodNode(Object node) {
		return node instanceof MethodNode;
	}

	public static boolean isFieldNode(Object node) {
		return node instanceof FieldNode;
	}

	public static Object getJavaNode(Object node) {
		if (node instanceof ClassNode) {
			return ((ClassNode) node).getJavaNode();
		} else if (node instanceof MethodNode) {
			return ((MethodNode) node).getJavaNode();
		} else if (node instanceof FieldNode) {
			return ((FieldNode) node).getJavaNode();
		}
		return null;
	}
}

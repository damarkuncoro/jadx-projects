package dexforge.core.parser.dex.decompiler;

import dexforge.core.parser.analysis.cfg.ControlFlowGraph;
import dexforge.core.parser.analysis.dataflow.DataFlowAnalyzer;
import dexforge.core.parser.analysis.dataflow.engine.DalvikRegisterFact;
import dexforge.core.parser.dex.decompiler.analysis.DominatorTree;
import dexforge.core.parser.dex.decompiler.analysis.SSATransformer;
import dexforge.core.parser.dex.decompiler.model.JavaClass;
import dexforge.core.parser.dex.decompiler.model.JavaField;
import dexforge.core.parser.dex.decompiler.model.JavaMethod;
import dexforge.core.parser.dex.decompiler.model.statements.*;
import dexforge.core.parser.dex.decompiler.model.expressions.*;
import dexforge.core.parser.dex.model.DexAnnotation;
import dexforge.core.parser.dex.model.DexClass;
import dexforge.core.parser.dex.model.DexEncodedField;
import dexforge.core.parser.dex.model.DexEncodedMethod;
import dexforge.core.parser.dex.model.DexInstruction;
import dexforge.core.parser.dex.service.DexFastIndexer;
import dexforge.core.parser.dex.sections.DexOpcode;
import dexforge.core.parser.kotlin.KotlinMetadataEnhancer;
import dexforge.core.parser.kotlin.model.KotlinFunctionInfo;
import dexforge.core.service.annotation.CodeInsightAnnotator;
import dexforge.core.service.intelligence.registry.DeobfuscationRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * High-level decompiler engine to translate bytecode into structured Java source code.
 */
public final class JavaDecompiler {
	private final DexFastIndexer indexer;
	private final VariableManager variableManager = new VariableManager();
	private final CodeInsightAnnotator annotator;
	private final KotlinMetadataEnhancer kotlinEnhancer;
	private DeobfuscationRegistry deobfRegistry;
	private Map<String, String> deobfuscationMap = new HashMap<>();

	public JavaDecompiler(DexFastIndexer indexer) {
		this.indexer = indexer;
		this.annotator = new CodeInsightAnnotator(indexer);
		this.kotlinEnhancer = new KotlinMetadataEnhancer(indexer);
	}

	public void setDeobfuscationRegistry(DeobfuscationRegistry registry) {
		this.deobfRegistry = registry;
	}

	public void setDeobfuscationMap(Map<String, String> map) {
		this.deobfuscationMap = map;
	}

	public void setJniBridges(Map<String, String> bridges) {
		this.annotator.setJniBridges(bridges);
	}

	public JavaClass decompileClass(DexClass dexClass) {
		String fullName = dexClass.getName();
		if (fullName.startsWith("L") && fullName.endsWith(";")) {
			fullName = fullName.substring(1, fullName.length() - 1);
		}
		String[] parts = fullName.split("/");
		String className = parts[parts.length - 1];
		String packageName = fullName.substring(0, Math.max(0, fullName.length() - className.length() - 1)).replace('/', '.');

		JavaClass javaClass = new JavaClass(packageName, className, dexClass.getSuperclass(), dexClass.getAccessFlags());

		// Add Kotlin-specific enhancements
		List<KotlinFunctionInfo> kotlinMetadata = kotlinEnhancer.extractMetadata(dexClass);
		if (!kotlinMetadata.isEmpty()) {
			javaClass.getAnnotations().add("// Kotlin Metadata Detected: Recovered " + kotlinMetadata.size() + " functions.");
		}

		// Add Class Annotations
		for (DexAnnotation ann : indexer.getClassAnnotations(dexClass)) {
			javaClass.getAnnotations().add(formatAnnotation(ann));
		}

		// Detect and add Inner Classes
		List<DexClass> inners = indexer.getInnerClasses(dexClass);
		for (DexClass inner : inners) {
			javaClass.getInnerClasses().add(decompileClass(inner));
		}

		// Process fields
		indexer.fillClassData(dexClass);
		if (dexClass.getClassData() != null) {
			for (DexEncodedField field : dexClass.getClassData().staticFields) {
				JavaField javaField = new JavaField(indexer.getFieldPool().getFieldName(field.getFieldIndex()),
						indexer.getFieldPool().getFieldType(field.getFieldIndex()), field.getAccessFlags());
				for (DexAnnotation ann : indexer.getFieldAnnotations(dexClass, field)) {
					javaField.getAnnotations().add(formatAnnotation(ann));
				}
				javaClass.getFields().add(javaField);
			}
			for (DexEncodedField field : dexClass.getClassData().instanceFields) {
				JavaField javaField = new JavaField(indexer.getFieldPool().getFieldName(field.getFieldIndex()),
						indexer.getFieldPool().getFieldType(field.getFieldIndex()), field.getAccessFlags());
				for (DexAnnotation ann : indexer.getFieldAnnotations(dexClass, field)) {
					javaField.getAnnotations().add(formatAnnotation(ann));
				}
				javaClass.getFields().add(javaField);
			}

			// Process methods
			for (DexEncodedMethod method : dexClass.getClassData().directMethods) {
				String mName = indexer.getMethodPool().getMethodName(method.getMethodIndex());
				String mSig = indexer.getMethodPool().getMethodSignature(method.getMethodIndex());
				JavaMethod javaMethod = decompileMethod(mName, mSig, true, 0, null); // Simplified
				for (DexAnnotation ann : indexer.getMethodAnnotations(dexClass, method)) {
					javaMethod.getAnnotations().add(formatAnnotation(ann));
				}
				javaClass.getMethods().add(javaMethod);
			}
		}

		return javaClass;
	}

	private String formatAnnotation(DexAnnotation ann) {
		String type = ann.getType();
		if (type.startsWith("L") && type.endsWith(";")) {
			type = type.substring(1, type.length() - 1).replace('/', '.');
		}

		if (type.endsWith("Override")) {
			return "@Override";
		}
		if (type.endsWith("Nullable")) {
			return "@Nullable";
		}
		if (type.endsWith("NonNull")) {
			return "@NonNull";
		}

		StringBuilder sb = new StringBuilder("@").append(type);
		if (!ann.getElements().isEmpty()) {
			sb.append("(...)");
		}
		return sb.toString();
	}

	public JavaMethod decompileMethod(String name, String signature, boolean isStatic, int totalRegs, ControlFlowGraph cfg) {
		if (cfg == null) {
			return new JavaMethod(name, "void");
		}

		List<String> paramTypes = parseParameterTypes(signature);
		variableManager.initParameters(isStatic, paramTypes, totalRegs);

		DominatorTree domTree = new DominatorTree(cfg);
		domTree.compute();

		SSATransformer ssa = new SSATransformer(cfg);
		ssa.transform();

		DataFlowAnalyzer dfa = new DataFlowAnalyzer(cfg);
		dfa.analyze();

		JavaMethod method = new JavaMethod(name, "void");

		ControlFlowRestructurer restructurer = new ControlFlowRestructurer();
		List<JavaStatement> structuredStatements = restructurer.restructure(cfg, this::translateToExpression, dfa);

		for (JavaStatement stmt : structuredStatements) {
			method.getStatements().add(stmt);
		}

		return method;
	}

	private List<String> parseParameterTypes(String signature) {
		List<String> types = new ArrayList<>();
		int start = signature.indexOf('(') + 1;
		int end = signature.indexOf(')');
		if (start < 1 || end < 0) {
			return types;
		}

		String params = signature.substring(start, end);
		for (int i = 0; i < params.length(); i++) {
			char c = params.charAt(i);
			if (c == 'L') {
				int semi = params.indexOf(';', i);
				types.add(params.substring(i, semi + 1));
				i = semi;
			} else if (c == '[') {
				if (params.length() > i + 1 && params.charAt(i + 1) == 'L') {
					int semi = params.indexOf(';', i);
					types.add(params.substring(i, semi + 1));
					i = semi;
				} else {
					types.add(params.substring(i, i + 2));
					i++;
				}
			} else {
				types.add(String.valueOf(c));
			}
		}
		return types;
	}

	public JavaExpression translateToExpression(DexInstruction insn, DalvikRegisterFact state) {
		int op = insn.getOpcode() & 0xFF;
		int reg = insn.getIndex();
		String type = state != null ? state.getType(reg) : null;
		String varName = variableManager.getVariableName(reg, type);

		if (op == 0x1A || op == 0x1B) {
			String val = indexer.getStringPool().getString(insn.getIndex());
			return new LiteralExpression(val);
		}
		if (op >= 0x12 && op <= 0x15) {
			return new LiteralExpression(insn.getIndex());
		}
		if (DexOpcode.isArithmetic(op)) {
			String operator = getArithmeticOperator(op);
			return new BinaryExpression(new VariableExpression(varName), operator, new VariableExpression(variableManager.getVariableName(reg + 1, type)));
		}
		if (DexOpcode.isBitwise(op)) {
			String operator = getBitwiseOperator(op);
			if (op == 0x7C) {
				return new UnaryExpression("~", new VariableExpression(varName), false);
			}
			return new BinaryExpression(new VariableExpression(varName), operator, new VariableExpression(variableManager.getVariableName(reg + 1, type)));
		}
		if (DexOpcode.isComparison(op)) {
			String operator = getComparisonOperator(op);
			return new BinaryExpression(new VariableExpression(varName), operator, new VariableExpression(variableManager.getVariableName(reg + 1, type)));
		}

		return new VariableExpression(varName);
	}

	public JavaStatement translateInstruction(DexInstruction insn, DalvikRegisterFact state) {
		int op = insn.getOpcode() & 0xFF;
		int reg = insn.getIndex();
		String type = state != null ? state.getType(reg) : null;
		String varName = variableManager.getVariableName(reg, type);
		VariableExpression varExpr = new VariableExpression(varName);

		if (op == 0x1A || op == 0x1B || (op >= 0x12 && op <= 0x15) || DexOpcode.isArithmetic(op) || DexOpcode.isBitwise(op)) {
			JavaStatement stmt = new AssignmentStatement(varExpr, translateToExpression(insn, state), type, false);
			String insight = annotator.getInsightComment(insn);
			if (!insight.isEmpty()) {
				return new BasicStatement(stmt.toCode(0) + insight);
			}
			return stmt;
		}
		if (op == 0x22) {
			String typeName = indexer.getTypePool().getTypeName(insn.getIndex());
			String deobfName = deobfuscationMap.getOrDefault(typeName, typeName);
			return new AssignmentStatement(varExpr, new NewInstanceExpression(deobfName), "Object", true);
		}
		if (op == 0x0E) {
			return new ReturnStatement("void");
		}
		if (op >= 0x0F && op <= 0x11) {
			return new ReturnStatement(varName);
		}
		if (op >= 0x6E && op <= 0x72) {
			String mName = indexer.getMethodPool().getMethodName(insn.getIndex());
			String mSig = indexer.getMethodPool().getMethodSignature(insn.getIndex());
			String objName = (op == 0x70) ? "super" : "this";

			// AST Rewrite: If this is a call to a known deobfuscator, replace with the result
			if (deobfRegistry != null) {
				String callKey = mSig + "@" + insn.getOffset(); // In real app use IP-DFA context
				Object resolved = deobfRegistry.getCachedValue(callKey);
				if (resolved instanceof String) {
					return new BasicStatement("// Resolved: \"" + resolved + "\"");
					// In full implementation: return new AssignmentStatement(varExpr, new LiteralExpression(resolved)...);
				}
			}

			return new BasicStatement(new MethodCallExpression(objName, mName).toCode());
		}

		return new BasicStatement("// " + insn.toString());
	}

	private String getArithmeticOperator(int op) {
		if ((op >= 0x90 && op <= 0x95) || (op >= 0xB0 && op <= 0xB5) || (op >= 0xD8 && op <= 0xDF)) {
			return "+";
		}
		if ((op >= 0x96 && op <= 0x9B) || (op >= 0xB6 && op <= 0xBB) || (op >= 0xE0 && op <= 0xE2)) {
			return "-";
		}
		if ((op >= 0x9C && op <= 0xA1) || (op >= 0xBC && op <= 0xC1) || (op >= 0xE3 && op <= 0xE5)) {
			return "*";
		}
		if ((op >= 0xA2 && op <= 0xA7) || (op >= 0xC2 && op <= 0xC7) || (op >= 0xE6 && op <= 0xE8)) {
			return "/";
		}
		if ((op >= 0xA8 && op <= 0xAD) || (op >= 0xC8 && op <= 0xCD)) {
			return "%";
		}
		return "+";
	}

	private String getBitwiseOperator(int op) {
		if (op == 0x9C || op == 0xBC || op == 0x81) {
			return "&";
		}
		if (op == 0x9D || op == 0xBD || op == 0x82) {
			return "|";
		}
		if (op == 0x9E || op == 0xBE || op == 0x83) {
			return "^";
		}
		if (op == 0x9F || op == 0xBF || op == 0x84) {
			return "<<";
		}
		if (op == 0xA0 || op == 0xC0 || op == 0x85) {
			return ">>";
		}
		if (op == 0xA1 || op == 0xC1 || op == 0x86) {
			return ">>>";
		}
		return "&";
	}

	private String getComparisonOperator(int op) {
		switch (op) {
			case 0x32:
			case 0x38:
				return "==";
			case 0x33:
			case 0x39:
				return "!=";
			case 0x34:
			case 0x3A:
				return "<";
			case 0x35:
			case 0x3B:
				return ">=";
			case 0x36:
			case 0x3C:
				return ">";
			case 0x37:
			case 0x3D:
				return "<=";
			default:
				return "==";
		}
	}
}

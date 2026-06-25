package dexforge.core.parser.dex.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import dexforge.core.parser.dex.io.DexByteReader;
import dexforge.core.parser.dex.model.DexClass;
import dexforge.core.parser.dex.model.DexCode;
import dexforge.core.parser.dex.model.DexEncodedMethod;
import dexforge.core.parser.dex.model.DexEncodedField;
import dexforge.core.parser.dex.model.DexHeader;
import dexforge.core.parser.dex.model.DexInstruction;
import dexforge.core.parser.dex.model.DexXref;
import dexforge.core.parser.dex.sections.DexAnnotationParser;
import dexforge.core.parser.dex.sections.DexClassDataParser;
import dexforge.core.parser.dex.sections.DexClassPool;
import dexforge.core.parser.dex.sections.DexCodeParser;
import dexforge.core.parser.dex.sections.DexDebugInfoParser;
import dexforge.core.parser.dex.sections.DexFieldPool;
import dexforge.core.parser.dex.sections.DexHeaderParser;
import dexforge.core.parser.dex.sections.DexInstructionDecoder;
import dexforge.core.parser.dex.sections.DexMethodPool;
import dexforge.core.parser.dex.sections.DexProtoPool;
import dexforge.core.parser.dex.sections.DexResourceResolver;
import dexforge.core.parser.dex.sections.DexStringPool;
import dexforge.core.parser.dex.sections.DexTypePool;
import dexforge.core.parser.smali.service.SmaliWriter;
import dexforge.core.parser.dex.analysis.DexMethodAnalyzer;
import dexforge.core.parser.resolver.ResourceResolver;

/**
 * Service for high-speed indexing of DEX metadata.
 * Designed to provide results long before a full decompiler engine finishes.
 */
public final class DexFastIndexer {
	private final DexByteReader reader;
	private final DexHeader header;
	private final DexStringPool stringPool;
	private final DexTypePool typePool;
	private final DexProtoPool protoPool;
	private final DexFieldPool fieldPool;
	private final DexMethodPool methodPool;
	private final DexClassPool classPool;
	private final DexClassDataParser classDataParser;
	private final DexCodeParser codeParser;
	private final DexAnnotationParser annotationParser;
	private final DexResourceResolver resourceResolver;
	private final DexDebugInfoParser debugInfoParser;
	private final SmaliWriter smaliWriter;
	private final DexMethodAnalyzer methodAnalyzer;
	private final ResourceResolver unifiedResourceResolver;

	// XREF Cache: String Index -> List of Methods
	private Map<Integer, List<DexXref>> stringXrefMap;
	// XREF Cache: Method Index -> List of Methods
	private Map<Integer, List<DexXref>> methodXrefMap;
	// XREF Cache: Field Index -> List of Methods
	private Map<Integer, List<DexXref>> fieldXrefMap;
	// XREF Cache: Type Index -> List of Methods
	private Map<Integer, List<DexXref>> typeXrefMap;
	// XREF Cache: Resource ID -> List of Methods
	private Map<Integer, List<DexXref>> resourceXrefMap;

	public DexFastIndexer(byte[] dexData) {
		this(dexData, new ResourceResolver());
	}

	public DexFastIndexer(byte[] dexData, ResourceResolver unifiedResourceResolver) {
		this.reader = new DexByteReader(dexData);
		this.unifiedResourceResolver = unifiedResourceResolver;
		this.header = new DexHeaderParser(reader).parse();
		this.stringPool = new DexStringPool(reader, header);
		this.typePool = new DexTypePool(reader, header, stringPool);
		this.protoPool = new DexProtoPool(reader, header, typePool, stringPool);
		this.fieldPool = new DexFieldPool(reader, header, typePool, stringPool);
		this.methodPool = new DexMethodPool(reader, header, typePool, stringPool, protoPool);
		this.classPool = new DexClassPool(reader, header, typePool, stringPool);
		this.classDataParser = new DexClassDataParser(reader);
		this.codeParser = new DexCodeParser(reader);
		this.annotationParser = new DexAnnotationParser(reader, typePool, stringPool);
		this.resourceResolver = new DexResourceResolver(reader);
		this.debugInfoParser = new DexDebugInfoParser(reader, stringPool, typePool);
		this.smaliWriter = new SmaliWriter(this);
		this.methodAnalyzer = new DexMethodAnalyzer(this);
	}

	/**
	 * Populates class with its fields and methods.
	 */
	public void fillClassData(DexClass dexClass) {
		if (dexClass.getClassData() != null || dexClass.getClassDataOff() == 0) {
			return;
		}
		dexClass.setClassData(classDataParser.parse(dexClass.getClassDataOff()));
	}

	/**
	 * Returns detailed information about all classes defined in the DEX.
	 */
	public List<DexClass> getClasses() {
		int count = classPool.getSize();
		List<DexClass> classes = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			classes.add(classPool.getClassDef(i));
		}
		return classes;
	}

	/**
	 * Returns all annotations for a given class.
	 */
	public List<dexforge.core.parser.dex.model.DexAnnotation> getClassAnnotations(DexClass clazz) {
		return annotationParser.getClassAnnotations(clazz.getAnnotationsOff());
	}

	public List<dexforge.core.parser.dex.model.DexAnnotation> getMethodAnnotations(DexClass clazz, DexEncodedMethod method) {
		return annotationParser.getMethodAnnotations(clazz.getAnnotationsOff(), method.getMethodIndex());
	}

	public List<dexforge.core.parser.dex.model.DexAnnotation> getFieldAnnotations(DexClass clazz, DexEncodedField field) {
		return annotationParser.getFieldAnnotations(clazz.getAnnotationsOff(), field.getFieldIndex());
	}

	/**
	 * Pre-builds the resource ID mapping by scanning R classes.
	 */
	public void buildResourceMap() {
		resourceResolver.layout(classPool, fieldPool, classDataParser);
	}

	/**
	 * Resolves an integer resource ID to its name (e.g. R.string.app_name).
	 */
	public String resolveResource(int resId) {
		buildResourceMap();
		return resourceResolver.resolve(resId);
	}

	/**
	 * Returns the Smali representation of a class.
	 */
	public String getSmali(DexClass clazz) {
		return smaliWriter.writeClass(clazz);
	}

	/**
	 * Returns debug information (line numbers and local variables) for a method.
	 */
	public DexDebugInfoParser.DebugInfo getDebugInfo(DexEncodedMethod method) {
		if (method.getCodeOff() == 0) {
			return new DexDebugInfoParser.DebugInfo(Collections.emptyMap(), Collections.emptyList());
		}
		DexCode code = codeParser.parse(method.getCodeOff());
		return debugInfoParser.parse(code.getDebugInfoOff());
	}

	/**
	 * Returns line number mapping for a method.
	 * (Bytecode Offset -> Line Number)
	 */
	public Map<Integer, Integer> getLineNumbers(DexEncodedMethod method) {
		return getDebugInfo(method).lineNumbers;
	}

	/**
	 * Finds all methods that have a local variable with the given name.
	 */
	public List<String> findMethodsWithLocalVariable(String varName) {
		List<String> results = new ArrayList<>();
		for (DexClass clazz : getClasses()) {
			fillClassData(clazz);
			var data = clazz.getClassData();
			if (data == null) {
				continue;
			}

			scanMethodsForVar(data.directMethods, varName, results);
			scanMethodsForVar(data.virtualMethods, varName, results);
		}
		return results;
	}

	private void scanMethodsForVar(List<DexEncodedMethod> methods, String varName, List<String> results) {
		for (var m : methods) {
			var debug = getDebugInfo(m);
			for (var v : debug.localVars) {
				if (v.name.equals(varName)) {
					results.add(methodPool.getMethodSignature(m.getMethodIndex()));
					break;
				}
			}
		}
	}

	/**
	 * Extracts the generic signature of a class if it exists.
	 * (e.g. "Ljava/util/List<Ljava/lang/String;>;")
	 */
	public String getClassSignature(DexClass clazz) {
		List<dexforge.core.parser.dex.model.DexAnnotation> annotations = getClassAnnotations(clazz);
		for (var ann : annotations) {
			if (ann.getType().equals("Ldalvik/annotation/Signature;")) {
				// The signature is usually stored as an array of strings in the 'value' element
				// In our current simple parser, we need to implement element parsing to get it.
				// For now, we mark this as an available metadata point.
				return "Generic Signature Present";
			}
		}
		return null;
	}

	/**
	 * Extracts all class names defined in the DEX file in milliseconds.
	 */
	public List<String> getClassNames() {
		int count = classPool.getSize();
		List<String> classNames = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			classNames.add(classPool.getClassDef(i).getName());
		}
		return classNames;
	}

	/**
	 * Returns all method signatures defined inside a specific class.
	 */
	public List<String> getMethodsInClass(DexClass dexClass) {
		fillClassData(dexClass);
		DexClassDataParser.ClassData data = dexClass.getClassData();
		if (data == null) {
			return Collections.emptyList();
		}

		List<String> methods = new ArrayList<>();
		for (var m : data.directMethods) {
			methods.add(methodPool.getMethodSignature(m.getMethodIndex()));
		}
		for (var m : data.virtualMethods) {
			methods.add(methodPool.getMethodSignature(m.getMethodIndex()));
		}
		return methods;
	}

	/**
	 * Analyzes a method to find calls where a specific constant value is used as an argument.
	 */
	public List<String> findCallsWithConstant(String methodPattern, String constantValue) {
		List<String> results = new ArrayList<>();
		for (DexClass clazz : getClasses()) {
			fillClassData(clazz);
			var data = clazz.getClassData();
			if (data == null) {
				continue;
			}

			scanMethodsForConstantArg(data.directMethods, methodPattern, constantValue, results);
			scanMethodsForConstantArg(data.virtualMethods, methodPattern, constantValue, results);
		}
		return results;
	}

	private void scanMethodsForConstantArg(List<DexEncodedMethod> methods, String pattern, String val, List<String> results) {
		for (var m : methods) {
			if (m.getCodeOff() == 0) {
				continue;
			}
			DexCode code = codeParser.parse(m.getCodeOff());
			var analyzedCalls = methodAnalyzer.analyzeCalls(code);
			for (var call : analyzedCalls) {
				if (call.methodSignature.contains(pattern)) {
					// Check if any argument matches our constant
					for (var arg : call.arguments) {
						if (val.equals(arg.value)) {
							results.add(methodPool.getMethodSignature(m.getMethodIndex()));
							break;
						}
					}
				}
			}
		}
	}

	/**
	 * Returns all field signatures defined inside a specific class.
	 */
	public List<String> getFieldsInClass(DexClass dexClass) {
		fillClassData(dexClass);
		DexClassDataParser.ClassData data = dexClass.getClassData();
		if (data == null) {
			return Collections.emptyList();
		}

		List<String> fields = new ArrayList<>();
		for (var f : data.staticFields) {
			fields.add(fieldPool.getDeclaringClass(f.getFieldIndex()) + "->" + fieldPool.getFieldName(f.getFieldIndex()));
		}
		for (var f : data.instanceFields) {
			fields.add(fieldPool.getDeclaringClass(f.getFieldIndex()) + "->" + fieldPool.getFieldName(f.getFieldIndex()));
		}
		return fields;
	}

	/**
	 * Finds all strings used within a specific method.
	 */
	public List<String> getStringsUsedInMethod(DexEncodedMethod method) {
		if (method.getCodeOff() == 0) {
			return Collections.emptyList();
		}

		DexCode code = codeParser.parse(method.getCodeOff());
		List<DexInstruction> insns = DexInstructionDecoder.decode(code);

		List<String> strings = new ArrayList<>();
		for (DexInstruction insn : insns) {
			int op = insn.getOpcode();
			if (op == 0x1A || op == 0x1B) { // CONST_STRING, CONST_STRING_JUMBO
				strings.add(stringPool.getString(insn.getIndex()));
			}
		}
		return strings;
	}

	/**
	 * Finds all method calls made within a specific method.
	 */
	public List<String> getMethodCallsInMethod(DexEncodedMethod method) {
		if (method.getCodeOff() == 0) {
			return Collections.emptyList();
		}

		DexCode code = codeParser.parse(method.getCodeOff());
		List<DexInstruction> insns = DexInstructionDecoder.decode(code);

		List<String> calls = new ArrayList<>();
		for (DexInstruction insn : insns) {
			int op = insn.getOpcode();
			if (op >= 0x6E && op <= 0x78) { // INVOKE-*
				int mIdx = insn.getIndex();
				if (mIdx >= 0 && mIdx < methodPool.getSize()) {
					calls.add(methodPool.getMethodSignature(mIdx));
				}
			}
		}
		return calls;
	}

	/**
	 * Extracts all method names referenced in the DEX file.
	 */
	public List<String> getMethodNames() {
		int count = methodPool.getSize();
		List<String> names = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			names.add(methodPool.getMethodName(i));
		}
		return names;
	}

	/**
	 * Extracts all method signatures referenced in the DEX file.
	 */
	public List<String> getMethodSignatures() {
		int count = methodPool.getSize();
		List<String> signatures = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			signatures.add(methodPool.getMethodSignature(i));
		}
		return signatures;
	}

	/**
	 * Extracts all field names referenced in the DEX file.
	 */
	public List<String> getFieldNames() {
		int count = fieldPool.getSize();
		List<String> names = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			names.add(fieldPool.getFieldName(i));
		}
		return names;
	}

	/**
	 * Finds all inner classes for a given outer class.
	 */
	public List<DexClass> getInnerClasses(DexClass outerClass) {
		String prefix = outerClass.getName().substring(0, outerClass.getName().length() - 1) + "$";
		List<DexClass> inner = new ArrayList<>();
		for (DexClass clazz : getClasses()) {
			if (clazz.getName().startsWith(prefix)) {
				inner.add(clazz);
			}
		}
		return inner;
	}

	/**
	 * Finds all strings in the string pool that contain the given query.
	 */
	public List<String> searchStrings(String query) {
		List<String> results = new ArrayList<>();
		int count = stringPool.getSize();
		for (int i = 0; i < count; i++) {
			String s = stringPool.getString(i);
			if (s.contains(query)) {
				results.add(s);
			}
		}
		return results;
	}

	/**
	 * Scans the entire DEX file to build cross-reference maps.
	 * This is a one-time operation that enables instant XREF lookups.
	 */
	public void buildXrefCache() {
		if (stringXrefMap != null) {
			return;
		}

		stringXrefMap = new HashMap<>();
		methodXrefMap = new HashMap<>();
		fieldXrefMap = new HashMap<>();
		typeXrefMap = new HashMap<>();
		resourceXrefMap = new HashMap<>();

		for (DexClass clazz : getClasses()) {
			fillClassData(clazz);
			DexClassDataParser.ClassData data = clazz.getClassData();
			if (data == null) {
				continue;
			}

			scanMethodsForXref(clazz, data.directMethods);
			scanMethodsForXref(clazz, data.virtualMethods);
		}
	}

	private void scanMethodsForXref(DexClass clazz, List<DexEncodedMethod> methods) {
		for (DexEncodedMethod method : methods) {
			if (method.getCodeOff() == 0) {
				continue;
			}

			DexCode code = codeParser.parse(method.getCodeOff());
			List<DexInstruction> insns = DexInstructionDecoder.decode(code);
			DexXref xref = new DexXref(clazz, method);

			for (DexInstruction insn : insns) {
				processInstructionForXref(insn, xref);
			}
		}
	}

	private void processInstructionForXref(DexInstruction insn, DexXref xref) {
		int op = insn.getOpcode();
		int index = insn.getIndex();
		if (op == 0x1A || op == 0x1B) { // String ref
			if (index >= 0 && index < stringPool.getSize()) {
				stringXrefMap.computeIfAbsent(index, k -> new ArrayList<>()).add(xref);
			}
		} else if (op >= 0x6E && op <= 0x78) { // Method ref
			if (index >= 0 && index < methodPool.getSize()) {
				methodXrefMap.computeIfAbsent(index, k -> new ArrayList<>()).add(xref);
			}
		} else if (op >= 0x52 && op <= 0x5F) { // Field ref (IGET/IPUT/SGET/SPUT)
			if (index >= 0 && index < fieldPool.getSize()) {
				fieldXrefMap.computeIfAbsent(index, k -> new ArrayList<>()).add(xref);
			}
		} else if (op == 0x1C || op == 0x1F || op == 0x22) { // Type ref
			if (index >= 0 && index < typePool.getSize()) {
				typeXrefMap.computeIfAbsent(index, k -> new ArrayList<>()).add(xref);
			}
		} else if (op >= 0x12 && op <= 0x15) { // Potential Resource ID
			if (unifiedResourceResolver != null && unifiedResourceResolver.isResourceId(index)) {
				resourceXrefMap.computeIfAbsent(index, k -> new ArrayList<>()).add(xref);
			}
		}
	}

	/**
	 * Find all methods that use a string containing the query.
	 */
	public Map<String, List<String>> searchGlobalStringUsages(String query) {
		buildXrefCache();
		Map<String, List<String>> results = new HashMap<>();

		for (int i = 0; i < stringPool.getSize(); i++) {
			String s = stringPool.getString(i);
			if (s.contains(query)) {
				List<DexXref> xrefs = stringXrefMap.get(i);
				if (xrefs != null) {
					List<String> callers = new ArrayList<>();
					for (DexXref x : xrefs) {
						callers.add(methodPool.getMethodSignature(x.getMethod().getMethodIndex()));
					}
					results.put(s, callers);
				}
			}
		}
		return results;
	}

	/**
	 * Find all callers of methods that match the signature query.
	 */
	public Map<String, List<String>> searchGlobalMethodCalls(String methodQuery) {
		buildXrefCache();
		Map<String, List<String>> results = new HashMap<>();

		for (int i = 0; i < methodPool.getSize(); i++) {
			String sig = methodPool.getMethodSignature(i);
			if (sig.contains(methodQuery)) {
				List<DexXref> callers = methodXrefMap.get(i);
				if (callers != null) {
					List<String> callerSigs = new ArrayList<>();
					for (DexXref x : callers) {
						callerSigs.add(methodPool.getMethodSignature(x.getMethod().getMethodIndex()));
					}
					results.put(sig, callerSigs);
				}
			}
		}
		return results;
	}

	/**
	 * Find methods whose parameter list contains the specified type.
	 */
	public List<String> findMethodsWithParameter(String typeName) {
		List<String> results = new ArrayList<>();
		int count = methodPool.getSize();
		for (int i = 0; i < count; i++) {
			int protoIdx = methodPool.getProtoIndex(i);
			List<String> params = protoPool.getParameters(protoIdx);
			if (params.contains(typeName)) {
				results.add(methodPool.getMethodSignature(i));
			}
		}
		return results;
	}

	/**
	 * Find all methods that access fields matching the query.
	 */
	public Map<String, List<String>> searchGlobalFieldUsages(String fieldQuery) {
		buildXrefCache();
		Map<String, List<String>> results = new HashMap<>();

		for (int i = 0; i < fieldPool.getSize(); i++) {
			String fieldName = fieldPool.getFieldName(i);
			String declaringClass = fieldPool.getDeclaringClass(i);
			String fullField = declaringClass + "->" + fieldName;

			if (fullField.contains(fieldQuery)) {
				List<DexXref> callers = fieldXrefMap.get(i);
				if (callers != null) {
					List<String> callerSigs = new ArrayList<>();
					for (DexXref x : callers) {
						callerSigs.add(methodPool.getMethodSignature(x.getMethod().getMethodIndex()));
					}
					results.put(fullField, callerSigs);
				}
			}
		}
		return results;
	}

	/**
	 * Find all methods that reference types matching the query.
	 */
	public Map<String, List<String>> searchGlobalTypeUsages(String typeQuery) {
		buildXrefCache();
		Map<String, List<String>> results = new HashMap<>();

		for (int i = 0; i < typePool.getSize(); i++) {
			String typeName = typePool.getTypeName(i);
			if (typeName.contains(typeQuery)) {
				List<DexXref> usages = typeXrefMap.get(i);
				if (usages != null) {
					List<String> callerSigs = new ArrayList<>();
					for (DexXref x : usages) {
						callerSigs.add(methodPool.getMethodSignature(x.getMethod().getMethodIndex()));
					}
					results.put(typeName, callerSigs);
				}
			}
		}
		return results;
	}

	/**
	 * Find all methods that reference the given resource ID.
	 */
	public List<String> searchGlobalResourceUsages(int resId) {
		buildXrefCache();
		List<String> results = new ArrayList<>();
		List<DexXref> usages = resourceXrefMap.get(resId);
		if (usages != null) {
			for (DexXref x : usages) {
				results.add(methodPool.getMethodSignature(x.getMethod().getMethodIndex()));
			}
		}
		return results;
	}

	public Map<Integer, Integer> getResourceUsageStats() {
		buildXrefCache();
		Map<Integer, Integer> stats = new HashMap<>();
		resourceXrefMap.forEach((id, usages) -> stats.put(id, usages.size()));
		return stats;
	}

	public DexHeader getHeader() {
		return header;
	}

	public int getStringCount() {
		return stringPool.getSize();
	}

	public DexStringPool getStringPool() {
		return stringPool;
	}

	public DexTypePool getTypePool() {
		return typePool;
	}

	public DexMethodPool getMethodPool() {
		return methodPool;
	}

	public List<dexforge.core.parser.dex.model.DexInstruction> getMethodInstructions(String signature) {
		for (DexClass clazz : getClasses()) {
			fillClassData(clazz);
			if (clazz.getClassData() == null) {
				continue;
			}

			for (DexEncodedMethod m : clazz.getClassData().directMethods) {
				if (methodPool.getMethodSignature(m.getMethodIndex()).equals(signature)) {
					if (m.getCodeOff() == 0) {
						return Collections.emptyList();
					}
					return dexforge.core.parser.dex.sections.DexInstructionDecoder.decode(codeParser.parse(m.getCodeOff()));
				}
			}
			for (DexEncodedMethod m : clazz.getClassData().virtualMethods) {
				if (methodPool.getMethodSignature(m.getMethodIndex()).equals(signature)) {
					if (m.getCodeOff() == 0) {
						return Collections.emptyList();
					}
					return dexforge.core.parser.dex.sections.DexInstructionDecoder.decode(codeParser.parse(m.getCodeOff()));
				}
			}
		}
		return Collections.emptyList();
	}

	public DexFieldPool getFieldPool() {
		return fieldPool;
	}

	public DexCodeParser getCodeParser() {
		return codeParser;
	}

	public ResourceResolver getUnifiedResourceResolver() {
		return unifiedResourceResolver;
	}

	public DexByteReader getReader() {
		return reader;
	}
}

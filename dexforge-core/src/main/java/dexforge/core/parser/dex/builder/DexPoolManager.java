package dexforge.core.parser.dex.builder;

import dexforge.core.parser.dex.model.DexCode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * Manages unique IDs and their ordering for DEX generation.
 * Ensures that all pools (strings, types, methods, fields) are sorted according to DEX spec.
 */
public final class DexPoolManager {
	private final Set<String> strings = new TreeSet<>();
	private final Set<String> types = new TreeSet<>();
	private final List<ProtoId> protos = new ArrayList<>();
	private final List<FieldId> fields = new ArrayList<>();
	private final List<MethodId> methods = new ArrayList<>();
	private final List<ClassDefId> classDefs = new ArrayList<>();
	private final Map<MethodId, DexCode> methodCodeMap = new HashMap<>();

	public void addString(String s) {
		if (s != null) {
			strings.add(s);
		}
	}

	public void addType(String type) {
		if (type != null) {
			types.add(type);
			addString(type);
		}
	}

	public void addProto(String shorty, String returnType, List<String> parameters) {
		ProtoId proto = new ProtoId(shorty, returnType, parameters);
		if (!protos.contains(proto)) {
			protos.add(proto);
			addString(shorty);
			addType(returnType);
			for (String p : parameters) {
				addType(p);
			}
		}
		Collections.sort(protos);
	}

	public void addField(String className, String name, String type) {
		FieldId fid = new FieldId(className, name, type);
		if (!fields.contains(fid)) {
			fields.add(fid);
			addType(className);
			addString(name);
			addType(type);
		}
		Collections.sort(fields);
	}

	public void addMethod(String className, String name, String shorty, String returnType, List<String> parameters) {
		ProtoId proto = new ProtoId(shorty, returnType, parameters);
		addProto(shorty, returnType, parameters);
		MethodId mid = new MethodId(className, name, proto);
		if (!methods.contains(mid)) {
			methods.add(mid);
			addType(className);
			addString(name);
		}
		Collections.sort(methods);
	}

	public void setMethodCode(String className, String name, ProtoId proto, DexCode code) {
		MethodId mid = new MethodId(className, name, proto);
		methodCodeMap.put(mid, code);
	}

	public DexCode getMethodCode(MethodId mid) {
		return methodCodeMap.get(mid);
	}

	public void addClassDef(String className, int accessFlags, String superName, List<String> interfaces, String sourceFile) {
		ClassDefId def = new ClassDefId(className, accessFlags, superName, interfaces, sourceFile);
		if (!classDefs.contains(def)) {
			classDefs.add(def);
			addType(className);
			if (superName != null) {
				addType(superName);
			}
			for (String iface : interfaces) {
				addType(iface);
			}
			if (sourceFile != null) {
				addString(sourceFile);
			}
		}
	}

	public int getStringIndex(String s) {
		return new ArrayList<>(strings).indexOf(s);
	}

	public int getTypeIndex(String type) {
		return new ArrayList<>(types).indexOf(type);
	}

	public int getProtoIndex(ProtoId proto) {
		return protos.indexOf(proto);
	}

	public List<String> getSortedStrings() {
		return new ArrayList<>(strings);
	}

	public List<String> getSortedTypes() {
		return new ArrayList<>(types);
	}

	public List<ProtoId> getSortedProtos() {
		return protos;
	}

	public List<FieldId> getSortedFields() {
		return fields;
	}

	public List<MethodId> getSortedMethods() {
		return methods;
	}

	public List<ClassDefId> getClassDefs() {
		return classDefs;
	}

	public static final class ProtoId implements Comparable<ProtoId> {
		private final String shorty;
		private final String returnType;
		private final List<String> parameters;

		public ProtoId(String shorty, String returnType, List<String> parameters) {
			this.shorty = shorty;
			this.returnType = returnType;
			this.parameters = parameters;
		}

		public String getShorty() {
			return shorty;
		}

		public String getReturnType() {
			return returnType;
		}

		public List<String> getParameters() {
			return parameters;
		}

		@Override
		public int compareTo(ProtoId o) {
			int res = returnType.compareTo(o.returnType);
			if (res != 0) {
				return res;
			}
			return Integer.compare(parameters.size(), o.parameters.size());
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			ProtoId protoId = (ProtoId) o;
			return Objects.equals(shorty, protoId.shorty) && Objects.equals(returnType, protoId.returnType) && Objects.equals(parameters, protoId.parameters);
		}

		@Override
		public int hashCode() {
			return Objects.hash(shorty, returnType, parameters);
		}
	}

	public static final class FieldId implements Comparable<FieldId> {
		private final String className;
		private final String name;
		private final String type;

		public FieldId(String className, String name, String type) {
			this.className = className;
			this.name = name;
			this.type = type;
		}

		public String getClassName() {
			return className;
		}

		public String getName() {
			return name;
		}

		public String getType() {
			return type;
		}

		@Override
		public int compareTo(FieldId o) {
			int res = className.compareTo(o.className);
			if (res != 0) {
				return res;
			}
			res = name.compareTo(o.name);
			if (res != 0) {
				return res;
			}
			return type.compareTo(o.type);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			FieldId fieldId = (FieldId) o;
			return Objects.equals(className, fieldId.className) && Objects.equals(name, fieldId.name) && Objects.equals(type, fieldId.type);
		}

		@Override
		public int hashCode() {
			return Objects.hash(className, name, type);
		}
	}

	public static final class MethodId implements Comparable<MethodId> {
		private final String className;
		private final String name;
		private final ProtoId proto;

		public MethodId(String className, String name, ProtoId proto) {
			this.className = className;
			this.name = name;
			this.proto = proto;
		}

		public String getClassName() {
			return className;
		}

		public String getName() {
			return name;
		}

		public ProtoId getProto() {
			return proto;
		}

		@Override
		public int compareTo(MethodId o) {
			int res = className.compareTo(o.className);
			if (res != 0) {
				return res;
			}
			res = name.compareTo(o.name);
			if (res != 0) {
				return res;
			}
			return proto.compareTo(o.proto);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			MethodId methodId = (MethodId) o;
			return Objects.equals(className, methodId.className) && Objects.equals(name, methodId.name) && Objects.equals(proto, methodId.proto);
		}

		@Override
		public int hashCode() {
			return Objects.hash(className, name, proto);
		}
	}

	public static final class ClassDefId {
		private final String className;
		private final int accessFlags;
		private final String superName;
		private final List<String> interfaces;
		private final String sourceFile;

		public ClassDefId(String className, int accessFlags, String superName, List<String> interfaces, String sourceFile) {
			this.className = className;
			this.accessFlags = accessFlags;
			this.superName = superName;
			this.interfaces = interfaces;
			this.sourceFile = sourceFile;
		}

		public String getClassName() {
			return className;
		}

		public int getAccessFlags() {
			return accessFlags;
		}

		public String getSuperName() {
			return superName;
		}

		public List<String> getInterfaces() {
			return interfaces;
		}

		public String getSourceFile() {
			return sourceFile;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			ClassDefId that = (ClassDefId) o;
			return Objects.equals(className, that.className);
		}

		@Override
		public int hashCode() {
			return Objects.hash(className);
		}
	}
}

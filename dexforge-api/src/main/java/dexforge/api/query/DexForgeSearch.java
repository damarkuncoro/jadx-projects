package dexforge.api.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dexforge.api.core.DexForgeProject;
import dexforge.api.model.DexForgeClass;
import dexforge.api.model.DexForgeField;
import dexforge.api.model.DexForgeMethod;
import dexforge.api.model.DexForgeNode;

/**
 * Entry point for searching nodes in a project.
 */
public final class DexForgeSearch {
	private final DexForgeProject project;

	public DexForgeSearch(DexForgeProject project) {
		this.project = project;
	}

	public DexForgeQuery.ClassQuery classes() {
		return new ClassQueryImpl(project.getClasses().stream());
	}

	public DexForgeQuery.MethodQuery methods() {
		return new MethodQueryImpl(project.getClasses().stream()
				.flatMap(cls -> cls.getMethods().stream()));
	}

	public DexForgeQuery.FieldQuery fields() {
		return new FieldQueryImpl(project.getClasses().stream()
				.flatMap(cls -> cls.getFields().stream()));
	}

	private abstract static class BaseQuery<T extends DexForgeNode, Q extends DexForgeQuery<T>> implements DexForgeQuery<T> {
		protected Stream<T> stream;

		BaseQuery(Stream<T> stream) {
			this.stream = stream;
		}

		@Override
		public Q named(String pattern) {
			Pattern p = Pattern.compile(pattern);
			this.stream = stream.filter(node -> p.matcher(node.getName()).find() || p.matcher(node.getFullName()).find());
			return (Q) this;
		}

		@Override
		public Q filter(Predicate<T> predicate) {
			this.stream = stream.filter(predicate);
			return (Q) this;
		}

		@Override
		public Q limit(int count) {
			this.stream = stream.limit(count);
			return (Q) this;
		}

		@Override
		public List<T> findAll() {
			return stream.collect(Collectors.toList());
		}

		@Override
		public Optional<T> findFirst() {
			return stream.findFirst();
		}
	}

	private static final class ClassQueryImpl extends BaseQuery<DexForgeClass, DexForgeQuery.ClassQuery> implements DexForgeQuery.ClassQuery {
		ClassQueryImpl(Stream<DexForgeClass> stream) { super(stream); }

		@Override
		public ClassQuery inPackage(String packageName) {
			this.stream = stream.filter(cls -> cls.getPackageName().equals(packageName));
			return this;
		}

		@Override
		public ClassQuery extending(String superClassName) {
			// Basic implementation, could be enhanced with hierarchy check
			return this;
		}

		@Override
		public ClassQuery implementing(String interfaceName) {
			return this;
		}
	}

	private static final class MethodQueryImpl extends BaseQuery<DexForgeMethod, DexForgeQuery.MethodQuery> implements DexForgeQuery.MethodQuery {
		MethodQueryImpl(Stream<DexForgeMethod> stream) { super(stream); }

		@Override
		public MethodQuery withReturnType(String typeName) {
			this.stream = stream.filter(mth -> mth.getReturnType().contains(typeName));
			return this;
		}

		@Override
		public MethodQuery withArguments(String... argTypes) {
			return this;
		}

		@Override
		public MethodQuery constructorsOnly() {
			this.stream = stream.filter(DexForgeMethod::isConstructor);
			return this;
		}
	}

	private static final class FieldQueryImpl extends BaseQuery<DexForgeField, DexForgeQuery.FieldQuery> implements DexForgeQuery.FieldQuery {
		FieldQueryImpl(Stream<DexForgeField> stream) { super(stream); }

		@Override
		public FieldQuery ofType(String typeName) {
			this.stream = stream.filter(fld -> fld.getType().contains(typeName));
			return this;
		}
	}
}

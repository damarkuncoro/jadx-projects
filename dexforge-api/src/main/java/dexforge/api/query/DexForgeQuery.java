package dexforge.api.query;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import dexforge.api.model.DexForgeClass;
import dexforge.api.model.DexForgeField;
import dexforge.api.model.DexForgeMethod;
import dexforge.api.model.DexForgeNode;

/**
 * Fluent Query API for exploring the project nodes.
 * Better and more expressive than raw loops.
 */
public interface DexForgeQuery<T extends DexForgeNode> {

	/**
	 * Filter nodes by name (regex supported).
	 */
	DexForgeQuery<T> named(String pattern);

	/**
	 * Filter nodes by a custom predicate.
	 */
	DexForgeQuery<T> filter(Predicate<T> predicate);

	/**
	 * Limit the number of results.
	 */
	DexForgeQuery<T> limit(int count);

	/**
	 * Execute query and get all matches.
	 */
	List<T> findAll();

	/**
	 * Execute query and get the first match.
	 */
	Optional<T> findFirst();

	/**
	 * Sub-query interfaces for specific types.
	 */
	interface ClassQuery extends DexForgeQuery<DexForgeClass> {
		ClassQuery inPackage(String packageName);
		ClassQuery extending(String superClassName);
		ClassQuery implementing(String interfaceName);
	}

	interface MethodQuery extends DexForgeQuery<DexForgeMethod> {
		MethodQuery withReturnType(String typeName);
		MethodQuery withArguments(String... argTypes);
		MethodQuery constructorsOnly();
	}

	interface FieldQuery extends DexForgeQuery<DexForgeField> {
		FieldQuery ofType(String typeName);
	}
}

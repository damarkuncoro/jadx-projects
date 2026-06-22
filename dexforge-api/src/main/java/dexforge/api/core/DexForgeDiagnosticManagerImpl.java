package dexforge.api.core;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dexforge.api.diagnostic.DexForgeDiagnostic;
import dexforge.api.diagnostic.DexForgeDiagnosticManager;
import dexforge.api.diagnostic.DexForgeDiagnosticSeverity;
import dexforge.api.model.DexForgeClass;
import dexforge.api.model.DexForgeNode;

/**
 * Implementation of Diagnostic Manager that aggregates data from project nodes.
 */
final class DexForgeDiagnosticManagerImpl implements DexForgeDiagnosticManager {
	private final DexForgeProject project;

	DexForgeDiagnosticManagerImpl(DexForgeProject project) {
		this.project = project;
	}

	@Override
	public List<DexForgeDiagnostic> getAll() {
		List<DexForgeDiagnostic> all = new ArrayList<>();
		for (DexForgeClass cls : project.getClasses()) {
			all.addAll(cls.getDiagnostics());
		}
		// Add engine level warnings
		int engineWarns = project.getWarningsCount();
		if (engineWarns > 0) {
			all.add(DexForgeDiagnostic.builder(DexForgeDiagnosticSeverity.WARNING, "JADX engine reported " + engineWarns + " warnings")
					.source("engine")
					.suggest("Check log for details")
					.build());
		}
		return all;
	}

	@Override
	public List<DexForgeDiagnostic> getForNode(DexForgeNode node) {
		if (node instanceof DexForgeClass) {
			return ((DexForgeClass) node).getDiagnostics();
		}
		// For methods/fields, find their class diagnostics
		DexForgeClass declaringClass = node.getDeclaringClass();
		if (declaringClass != null) {
			return declaringClass.getDiagnostics().stream()
					.filter(d -> d.getRelatedNode().map(rn -> rn.getId().equals(node.getId())).orElse(false))
					.collect(Collectors.toList());
		}
		return java.util.Collections.emptyList();
	}

	@Override
	public List<DexForgeDiagnostic> getBySeverity(DexForgeDiagnosticSeverity severity) {
		return getAll().stream()
				.filter(d -> d.getSeverity() == severity)
				.collect(Collectors.toList());
	}

	@Override
	public int getErrorCount() {
		return project.getErrorsCount();
	}

	@Override
	public int getWarningCount() {
		return (int) getAll().stream().filter(d -> d.getSeverity() == DexForgeDiagnosticSeverity.WARNING).count();
	}
}

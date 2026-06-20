package dexforge.domain.model.source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import dexforge.domain.event.DomainEvent;
import dexforge.domain.model.AggregateRoot;
import dexforge.domain.model.project.ProjectId;

/**
 * Aggregate Root: SourceFile
 * Represents satu file sumber hasil decompilasi.
 */
public class SourceFile extends AggregateRoot {
	private final ProjectId projectId;
	private final String className;
	private final String sourceCode;
	private final List<MethodSignature> methods;
	private final List<String> imports;
	private final SourceFileStatus status;

	private SourceFile(SourceFileId id, ProjectId projectId, String className, String sourceCode) {
		super(id);
		this.projectId = Objects.requireNonNull(projectId, "ProjectId cannot be null");
		this.className = Objects.requireNonNull(className, "Class name cannot be null");
		this.sourceCode = sourceCode != null ? sourceCode : "";
		this.methods = new ArrayList<>();
		this.imports = new ArrayList<>();
		this.status = SourceFileStatus.LOADED;
	}

	public static SourceFile create(SourceFileId id, ProjectId projectId, String className, String sourceCode) {
		return new SourceFile(id, projectId, className, sourceCode);
	}

	public void addMethod(MethodSignature method) {
		Objects.requireNonNull(method, "Method cannot be null");
		if (status != SourceFileStatus.LOADED) {
			throw new IllegalStateException("Cannot add method to " + status + " source file");
		}
		methods.add(method);
		raise(new MethodAddedEvent((SourceFileId) id, method));
	}

	public void addImport(String importName) {
		Objects.requireNonNull(importName, "Import name cannot be null");
		imports.add(importName);
	}

	public void markAsAnalyzed() {
		if (status != SourceFileStatus.LOADED) {
			throw new IllegalStateException("Cannot analyze " + status + " source file");
		}
		raise(new SourceFileAnalyzedEvent((SourceFileId) id));
	}

	public SourceFileId getSourceFileId() {
		return (SourceFileId) id;
	}

	public ProjectId getProjectId() {
		return projectId;
	}

	public String getClassName() {
		return className;
	}

	public String getSourceCode() {
		return sourceCode;
	}

	public List<MethodSignature> getMethods() {
		return Collections.unmodifiableList(methods);
	}

	public List<String> getImports() {
		return Collections.unmodifiableList(imports);
	}

	public SourceFileStatus getStatus() {
		return status;
	}

	// ===== Domain Events =====

	public static class MethodAddedEvent implements DomainEvent {
		private final SourceFileId sourceFileId;
		private final MethodSignature method;
		private final long occurredAtMs;

		public MethodAddedEvent(SourceFileId sourceFileId, MethodSignature method) {
			this.sourceFileId = sourceFileId;
			this.method = method;
			this.occurredAtMs = System.currentTimeMillis();
		}

		@Override
		public String getEventType() {
			return "MethodAdded";
		}

		@Override
		public java.time.LocalDateTime getOccurredAt() {
			return java.time.LocalDateTime.ofInstant(
					java.time.Instant.ofEpochMilli(occurredAtMs),
					java.time.ZoneId.systemDefault());
		}

		public SourceFileId getSourceFileId() {
			return sourceFileId;
		}

		public MethodSignature getMethod() {
			return method;
		}
	}

	public static class SourceFileAnalyzedEvent implements DomainEvent {
		private final SourceFileId sourceFileId;
		private final long occurredAtMs;

		public SourceFileAnalyzedEvent(SourceFileId sourceFileId) {
			this.sourceFileId = sourceFileId;
			this.occurredAtMs = System.currentTimeMillis();
		}

		@Override
		public String getEventType() {
			return "SourceFileAnalyzed";
		}

		@Override
		public java.time.LocalDateTime getOccurredAt() {
			return java.time.LocalDateTime.ofInstant(
					java.time.Instant.ofEpochMilli(occurredAtMs),
					java.time.ZoneId.systemDefault());
		}

		public SourceFileId getSourceFileId() {
			return sourceFileId;
		}
	}
}

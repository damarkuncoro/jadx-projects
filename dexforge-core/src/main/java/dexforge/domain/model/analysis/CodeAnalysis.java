package dexforge.domain.model.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import dexforge.domain.model.AggregateRoot;
import dexforge.domain.model.source.SourceFileId;

/**
 * Aggregate Root: CodeAnalysis
 * Holds analysis results for a source file.
 */
public class CodeAnalysis extends AggregateRoot {
	private final SourceFileId sourceFileId;
	private final List<SecurityFinding> findings;

	private CodeAnalysis(SourceFileId sourceFileId, List<SecurityFinding> findings) {
		super(null); // Not using entity id for this aggregate
		this.sourceFileId = Objects.requireNonNull(sourceFileId);
		this.findings = findings != null ? new ArrayList<>(findings) : new ArrayList<>();
	}

	public static CodeAnalysis of(SourceFileId sourceFileId, List<SecurityFinding> findings) {
		return new CodeAnalysis(sourceFileId, findings);
	}

	public List<SecurityFinding> getFindings() {
		return Collections.unmodifiableList(findings);
	}

	public int getFindingCount() {
		return findings.size();
	}
}

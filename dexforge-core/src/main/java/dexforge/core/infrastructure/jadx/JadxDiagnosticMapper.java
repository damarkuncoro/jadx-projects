package dexforge.core.infrastructure.jadx;

import java.util.ArrayList;
import java.util.List;

import dexforge.engine.DexForgeDiagnostic;
import dexforge.engine.DexForgeDiagnosticSeverity;

import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.ILineAttributeNode;
import jadx.core.dex.attributes.nodes.JadxError;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.FieldNode;
import jadx.core.dex.nodes.IDexNode;
import jadx.core.dex.nodes.MethodNode;

public final class JadxDiagnosticMapper {
	private JadxDiagnosticMapper() {
	}

	public static List<DexForgeDiagnostic> collectDiagnostics(ClassNode classNode, String code) {
		List<DexForgeDiagnostic> list = new ArrayList<>();
		collectDiagnosticsRecursive(classNode, code, list);
		return list;
	}

	private static void collectDiagnosticsRecursive(ClassNode cls, String code, List<DexForgeDiagnostic> list) {
		for (JadxError err : cls.getAll(AType.JADX_ERROR)) {
			list.add(createDiagnostic(cls, err.getError(), code));
		}
		for (MethodNode mth : cls.getMethods()) {
			for (JadxError err : mth.getAll(AType.JADX_ERROR)) {
				list.add(createDiagnostic(mth, err.getError(), code));
			}
		}
		for (FieldNode fld : cls.getFields()) {
			for (JadxError err : fld.getAll(AType.JADX_ERROR)) {
				list.add(createDiagnostic(fld, err.getError(), code));
			}
		}
		for (ClassNode inner : cls.getInnerClasses()) {
			collectDiagnosticsRecursive(inner, code, list);
		}
	}

	private static DexForgeDiagnostic createDiagnostic(IDexNode node, String message, String code) {
		int defPos = 0;
		if (node instanceof ILineAttributeNode) {
			defPos = ((ILineAttributeNode) node).getDefPosition();
		}

		int line = 0;
		int column = 0;
		if (defPos > 0 && defPos < code.length()) {
			int pos = 0;
			while (pos < defPos && pos < code.length()) {
				if (code.charAt(pos) == '\n') {
					line++;
					column = 0;
				} else {
					column++;
				}
				pos++;
			}
		}

		return DexForgeDiagnostic.builder(DexForgeDiagnosticSeverity.ERROR, message)
				.source(node.toString())
				.position(line, column)
				.build();
	}
}

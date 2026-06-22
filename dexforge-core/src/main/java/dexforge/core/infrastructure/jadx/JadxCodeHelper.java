package dexforge.core.infrastructure.jadx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jadx.api.ICodeInfo;
import jadx.api.JavaNode;
import jadx.api.metadata.ICodeAnnotation;
import jadx.api.metadata.ICodeNodeRef;
import jadx.api.metadata.annotations.NodeDeclareRef;

/**
 * Internal helper to bridge JADX Code operations without exposing them to public API.
 */
public final class JadxCodeHelper {
	private JadxCodeHelper() {
	}

	public static Object emptyCodeInfo() {
		return ICodeInfo.EMPTY;
	}

	public static String getCode(Object codeInfo) {
		return ((ICodeInfo) codeInfo).getCodeStr();
	}

	public static boolean hasMetadata(Object codeInfo) {
		return ((ICodeInfo) codeInfo).hasMetadata();
	}

	public static Integer getSourceLine(Object codeInfo, int decompiledLine) {
		return ((ICodeInfo) codeInfo).getCodeMetadata().getLineMapping().get(decompiledLine);
	}

	public static List<Integer> getUsePlacesFor(Object codeInfo, Object javaNode) {
		if (javaNode instanceof JavaNode && ((ICodeInfo) codeInfo).hasMetadata()) {
			List<Integer> result = new ArrayList<>();
			((ICodeInfo) codeInfo).getCodeMetadata().searchDown(0, (pos, ann) -> {
				if (((JavaNode) javaNode).isOwnCodeAnnotation(ann)) {
					result.add(pos);
				}
				return null;
			});
			return result;
		}
		return Collections.emptyList();
	}

	public static Object getNodeAt(Object codeInfo, int position) {
		if (!hasMetadata(codeInfo)) {
			return null;
		}
		ICodeNodeRef nodeRef = ((ICodeInfo) codeInfo).getCodeMetadata().getNodeAt(position);
		return nodeRef != null ? nodeRef : null;
	}

	public static Map<Integer, Object> getDefinitions(Object codeInfo) {
		if (!hasMetadata(codeInfo)) {
			return Collections.emptyMap();
		}
		Map<Integer, ICodeAnnotation> annotations = ((ICodeInfo) codeInfo).getCodeMetadata().getAsMap();
		Map<Integer, Object> result = new HashMap<>();
		annotations.forEach((pos, ann) -> {
			if (ann instanceof NodeDeclareRef) {
				result.put(pos, ((NodeDeclareRef) ann).getNode());
			}
		});
		return result;
	}
}

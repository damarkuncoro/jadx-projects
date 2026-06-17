package jadx.core.dex.nodes;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.api.ICodeCache;
import jadx.api.ICodeInfo;
import jadx.api.impl.SimpleCodeInfo;
import jadx.api.metadata.ICodeAnnotation;
import jadx.api.metadata.annotations.NodeDeclareRef;
import jadx.api.metadata.annotations.VarRef;
import jadx.core.Consts;
import jadx.core.utils.Utils;

public class ClassDecompiler {
	private static final Logger LOG = LoggerFactory.getLogger(ClassDecompiler.class);

	public static ICodeInfo decompile(ClassNode cls, boolean searchInCache) {
		if (cls.isInner()) {
			return ICodeInfo.EMPTY;
		}
		ICodeCache codeCache = cls.root().getCodeCache();
		String clsRawName = cls.getRawName();
		if (searchInCache) {
			ICodeInfo code = codeCache.get(clsRawName);
			if (code != ICodeInfo.EMPTY) {
				return code;
			}
		}
		ICodeInfo codeInfo = generateClassCode(cls);
		if (codeInfo != ICodeInfo.EMPTY) {
			codeCache.add(clsRawName, codeInfo);
		}
		return codeInfo;
	}

	private static ICodeInfo generateClassCode(ClassNode cls) {
		try {
			if (Consts.DEBUG) {
				LOG.debug("Decompiling class: {}", cls);
			}
			ICodeInfo codeInfo = cls.root().getProcessClasses().generateCode(cls);
			processDefinitionAnnotations(codeInfo);
			return codeInfo;
		} catch (StackOverflowError | Exception e) {
			cls.addError("Code generation failed", e);
			return new SimpleCodeInfo(Utils.getStackTrace(e));
		}
	}

	/**
	 * Save node definition positions found in code
	 */
	public static void processDefinitionAnnotations(ICodeInfo codeInfo) {
		Map<Integer, ICodeAnnotation> annotations = codeInfo.getCodeMetadata().getAsMap();
		if (annotations.isEmpty()) {
			return;
		}
		for (Map.Entry<Integer, ICodeAnnotation> entry : annotations.entrySet()) {
			ICodeAnnotation ann = entry.getValue();
			if (ann.getAnnType() == ICodeAnnotation.AnnType.DECLARATION) {
				NodeDeclareRef declareRef = (NodeDeclareRef) ann;
				int pos = entry.getKey();
				declareRef.setDefPos(pos);
				declareRef.getNode().setDefPosition(pos);
			}
		}
		// validate var refs
		annotations.values().removeIf(v -> {
			if (v.getAnnType() == ICodeAnnotation.AnnType.VAR_REF) {
				VarRef varRef = (VarRef) v;
				if (varRef.getRefPos() == 0) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Var reference '{}' incorrect (ref pos is zero) and was removed from metadata", varRef);
					}
					return true;
				}
				return false;
			}
			return false;
		});
	}
}

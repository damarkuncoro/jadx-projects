package dexforge.core.infrastructure.jadx;

import jadx.api.ResourceFile;
import jadx.api.ResourceType;

/**
 * Internal helper to bridge JADX Resource operations without exposing them to public API.
 */
public final class JadxResourceHelper {
	private JadxResourceHelper() {
	}

	public static String getOriginalName(Object resourceFile) {
		return ((ResourceFile) resourceFile).getOriginalName();
	}

	public static String getDeobfName(Object resourceFile) {
		return ((ResourceFile) resourceFile).getDeobfName();
	}

	public static void setDeobfName(Object resourceFile, String name) {
		((ResourceFile) resourceFile).setDeobfName(name);
	}

	public static String getTypeName(Object resourceFile) {
		return ((ResourceFile) resourceFile).getType().name();
	}

	public static String getFileType(String fileName) {
		return ResourceType.getFileType(fileName).name();
	}

	public static Object loadContent(Object resourceFile) {
		return ((ResourceFile) resourceFile).loadContent();
	}

	public static String getResContainerName(Object resContainer) {
		return ((jadx.core.xmlgen.ResContainer) resContainer).getName();
	}

	public static String getResContainerDataType(Object resContainer) {
		return ((jadx.core.xmlgen.ResContainer) resContainer).getDataType().name();
	}

	public static String getResContainerText(Object resContainer) {
		jadx.core.xmlgen.ResContainer container = (jadx.core.xmlgen.ResContainer) resContainer;
		if (container.getDataType() == jadx.core.xmlgen.ResContainer.DataType.TEXT
				|| container.getDataType() == jadx.core.xmlgen.ResContainer.DataType.RES_TABLE) {
			return container.getText().getCodeStr();
		}
		return null;
	}

	public static byte[] getResContainerBinary(Object resContainer) {
		jadx.core.xmlgen.ResContainer container = (jadx.core.xmlgen.ResContainer) resContainer;
		if (container.getDataType() == jadx.core.xmlgen.ResContainer.DataType.DECODED_DATA) {
			return container.getDecodedData();
		}
		return null;
	}

	public static java.util.List<?> getResContainerSubFiles(Object resContainer) {
		return ((jadx.core.xmlgen.ResContainer) resContainer).getSubFiles();
	}
}

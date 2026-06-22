package dexforge.api;

import jadx.api.ResourceType;

public enum DexForgeResourceType {
	CODE(DexForgeResourceContentType.BINARY),
	XML(DexForgeResourceContentType.TEXT),
	ARSC(DexForgeResourceContentType.TEXT),
	APK(DexForgeResourceContentType.BINARY),
	FONT(DexForgeResourceContentType.BINARY),
	IMG(DexForgeResourceContentType.BINARY),
	ARCHIVE(DexForgeResourceContentType.BINARY),
	VIDEOS(DexForgeResourceContentType.BINARY),
	SOUNDS(DexForgeResourceContentType.BINARY),
	JSON(DexForgeResourceContentType.TEXT),
	TEXT(DexForgeResourceContentType.TEXT),
	HTML(DexForgeResourceContentType.TEXT),
	LIB(DexForgeResourceContentType.BINARY),
	MANIFEST(DexForgeResourceContentType.TEXT),
	UNKNOWN_BIN(DexForgeResourceContentType.BINARY),
	UNKNOWN(DexForgeResourceContentType.UNKNOWN);

	private final DexForgeResourceContentType contentType;

	DexForgeResourceType(DexForgeResourceContentType contentType) {
		this.contentType = contentType;
	}

	public DexForgeResourceContentType getContentType() {
		return contentType;
	}

	static DexForgeResourceType fromJadx(ResourceType type) {
		return DexForgeResourceType.valueOf(type.name());
	}

	ResourceType toJadx() {
		return ResourceType.valueOf(name());
	}

	public static DexForgeResourceType getFileType(String fileName) {
		return fromJadx(ResourceType.getFileType(fileName));
	}
}

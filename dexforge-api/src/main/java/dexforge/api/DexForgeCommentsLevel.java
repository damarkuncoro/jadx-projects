package dexforge.api;

import jadx.api.CommentsLevel;

public enum DexForgeCommentsLevel {
	NONE,
	USER_ONLY,
	ERROR,
	WARN,
	INFO,
	DEBUG;

	CommentsLevel toJadx() {
		return CommentsLevel.valueOf(name());
	}
}

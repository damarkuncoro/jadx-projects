package dexforge.engine.jadx.infrastructure;

import dexforge.api.core.DexForgeSettings;
import jadx.api.CommentsLevel;
import jadx.api.DecompilationMode;
import jadx.api.JadxArgs;

public final class JadxSettingsAdapter {

	public static void apply(DexForgeSettings settings, JadxArgs args) {
		args.setThreadsCount(settings.getThreadsCount());
		// Map other settings as needed
		// This is just a sample based on previous test expectations
		args.setSkipSources(settings.asMap().get("skipSources").equals(true));
		args.setSkipResources(settings.asMap().get("skipResources").equals(true));

		String commentsLevel = (String) settings.asMap().get("commentsLevel");
		if (commentsLevel != null) {
			args.setCommentsLevel(CommentsLevel.valueOf(commentsLevel));
		}

		String decompilationMode = (String) settings.asMap().get("decompilationMode");
		if (decompilationMode != null) {
			args.setDecompilationMode(DecompilationMode.valueOf(decompilationMode));
		}
	}

	private JadxSettingsAdapter() {}
}

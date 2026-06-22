package dexforge.api;

import java.io.File;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import dexforge.api.core.DexForgeCommentsLevel;
import dexforge.api.core.DexForgeDecompilationMode;
import dexforge.api.core.DexForgeDecompiler;
import dexforge.api.core.DexForgeProject;
import dexforge.api.core.DexForgeSettings;
import dexforge.api.exception.DexForgeException;

import jadx.api.JadxArgs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DexForgeSettingsTest {
	@Test
	void shouldApplySettingsToJadxArgs() {
		JadxArgs args = new JadxArgs();

		DexForgeSettings.builder()
				.threadsCount(8)
				.typeUpdatesLimit(42)
				.skipSources(true)
				.skipResources(true)
				.showInconsistentCode(false)
				.commentsLevel(DexForgeCommentsLevel.ERROR)
				.decompilationMode(DexForgeDecompilationMode.SIMPLE)
				.useDexForgeApi(true)
				.build()
				.applyTo(args);

		assertThat(args.getThreadsCount()).isEqualTo(8);
		assertThat(args.getTypeUpdatesLimitCount()).isEqualTo(42);
		assertThat(args.isSkipSources()).isTrue();
		assertThat(args.isSkipResources()).isTrue();
		assertThat(args.isShowInconsistentCode()).isFalse();
		assertThat(args.getCommentsLevel().name()).isEqualTo("ERROR");
		assertThat(args.getDecompilationMode().name()).isEqualTo("SIMPLE");
	}

	@Test
	void shouldRequireInputForNativeBuilder() {
		assertThatThrownBy(() -> DexForgeDecompiler.builder().build())
				.isInstanceOf(DexForgeException.class)
				.hasMessage("At least one input file is required");
	}

	@Test
	void shouldCreateDecompilerWithNativeBuilder() {
		DexForgeProject project = DexForgeDecompiler.builder()
				.inputFile(new File("sample.apk"))
				.settings(DexForgeSettings.builder().threadsCount(2).build())
				.build();

		try {
			assertThat(project.getSettings().getThreadsCount()).isEqualTo(2);
		} finally {
			project.close();
		}
	}
}

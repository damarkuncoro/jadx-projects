package dexforge.engine.jadx.session;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Collectors;

import dexforge.domain.model.project.Project;
import dexforge.domain.model.project.ProjectConfig;
import dexforge.domain.model.project.ProjectId;
import dexforge.engine.DexForgeOpenProjectRequest;
import dexforge.engine.DexForgeProjectSession;
import dexforge.engine.DexForgeProjectSessionFactory;
import jadx.api.JadxArgs;
import jadx.api.JadxDecompiler;

public final class JadxProjectSessionFactory implements DexForgeProjectSessionFactory {
	private final JadxArgs baseArgs;

	public JadxProjectSessionFactory(JadxArgs baseArgs) {
		this.baseArgs = Objects.requireNonNull(baseArgs);
	}

	@Override
	public DexForgeProjectSession open(DexForgeOpenProjectRequest request) {
		Path inputPath = request.getInputPath();
		JadxArgs args = new JadxArgs();
		args.getInputFiles().add(inputPath.toFile());

		JadxDecompiler decompiler = new JadxDecompiler(args);
		decompiler.load();

		Project project = Project.create(
				ProjectId.generate(),
				ProjectConfig.create("JADX Project", ""),
				inputPath
		);
		return new JadxProjectSession(decompiler, project);
	}
}

package dexforge.gui.application.usecase;

import java.util.Objects;
import dexforge.api.model.DexForgeApkMetadata;
import dexforge.gui.application.port.DecompilerPort;

public final class GetApkInfoUseCase {
    private final DecompilerPort decompilerPort;

    public GetApkInfoUseCase(DecompilerPort decompilerPort) {
        this.decompilerPort = Objects.requireNonNull(decompilerPort);
    }

    public DexForgeApkMetadata execute() {
        return decompilerPort.getApkMetadata();
    }
}

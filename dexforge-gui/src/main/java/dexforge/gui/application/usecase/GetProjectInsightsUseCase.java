package dexforge.gui.application.usecase;

import dexforge.api.intelligence.IProjectIntelligence;
import dexforge.gui.application.port.DecompilerPort;
import java.util.Map;
import java.util.HashMap;

public final class GetProjectInsightsUseCase {
    private final DecompilerPort decompilerPort;

    public GetProjectInsightsUseCase(DecompilerPort decompilerPort) {
        this.decompilerPort = decompilerPort;
    }

    public Map<String, Object> execute() {
        IProjectIntelligence intelligenceApi = decompilerPort.getIntelligence();
        if (intelligenceApi == null) return new HashMap<>();
        return intelligenceApi.getProjectInsights();
    }
}

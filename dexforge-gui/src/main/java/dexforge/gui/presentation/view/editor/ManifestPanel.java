package dexforge.gui.presentation.view.editor;

import dexforge.gui.presentation.view.CodeAreaPanel;
import dexforge.gui.presentation.viewmodel.MainViewModel;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

public final class ManifestPanel extends CodeAreaPanel {
    public ManifestPanel(MainViewModel viewModel) {
        super(viewModel, SyntaxConstants.SYNTAX_STYLE_XML);

        viewModel.onResourcesLoaded(resources -> {
            for (dexforge.api.resource.DexForgeResourceFile res : resources) {
                if ("AndroidManifest.xml".equals(res.getOriginalName())) {
                    setText(res.getContent());
                    break;
                }
            }
        });
    }
}

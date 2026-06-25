package dexforge.gui.presentation.view.manager;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntConsumer;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import dexforge.api.resource.DexForgeResourceFile;
import dexforge.gui.presentation.view.CodeAreaPanel;
import dexforge.gui.presentation.viewmodel.MainViewModel;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

/**
 * Manages multiple editor tabs for different files/classes.
 */
public final class EditorManager {
    private final JTabbedPane tabbedPane;
    private final MainViewModel viewModel;
    private final Map<String, CodeAreaPanel> openedEditors = new HashMap<>();
    private final Map<String, CodeAreaPanel> openedResources = new HashMap<>();

    public EditorManager(JTabbedPane tabbedPane, MainViewModel viewModel) {
        this.tabbedPane = tabbedPane;
        this.viewModel = viewModel;

        setupTabbedPane();
        bindViewModel();
    }

    private void setupTabbedPane() {
        tabbedPane.putClientProperty("JTabbedPane.tabClosable", true);
        tabbedPane.putClientProperty("JTabbedPane.tabCloseCallback", (IntConsumer) index -> {
            Component comp = tabbedPane.getComponentAt(index);
            if (comp instanceof JComponent) {
                JComponent component = (JComponent) comp;
                String className = (String) component.getClientProperty("className");
                if (className != null) {
                    openedEditors.remove(className);
                }
                String resourceName = (String) component.getClientProperty("resourceName");
                if (resourceName != null) {
                    openedResources.remove(resourceName);
                }
            }
            tabbedPane.removeTabAt(index);
        });
    }

    private void bindViewModel() {
        viewModel.onClassRequested(this::openClass);
        viewModel.onResourceRequested(this::openResource);

        viewModel.onCodeChanged(code -> {
            Component selected = tabbedPane.getSelectedComponent();
            if (selected instanceof CodeAreaPanel) {
                CodeAreaPanel current = (CodeAreaPanel) selected;
                current.setText(code);
            }
        });
    }

    public void openClass(String className) {
        if (openedEditors.containsKey(className)) {
            tabbedPane.setSelectedComponent(openedEditors.get(className));
            // Trigger selection in decompiler to sync state if needed
            viewModel.selectClass(className);
            return;
        }

        CodeAreaPanel editor = new CodeAreaPanel(viewModel);
        editor.putClientProperty("className", className);
        openedEditors.put(className, editor);

        String title = getSimpleClassName(className);
        tabbedPane.addTab(title, editor);
        tabbedPane.setSelectedComponent(editor);

        viewModel.selectClass(className);
    }

    public void openResource(DexForgeResourceFile resourceFile) {
        String resourceName = getResourceKey(resourceFile);
        if (openedResources.containsKey(resourceName)) {
            tabbedPane.setSelectedComponent(openedResources.get(resourceName));
            viewModel.selectResource(resourceFile);
            return;
        }

        CodeAreaPanel editor = new CodeAreaPanel(viewModel, syntaxForResource(resourceName));
        editor.putClientProperty("resourceName", resourceName);
        openedResources.put(resourceName, editor);

        tabbedPane.addTab(getFileName(resourceName), editor);
        tabbedPane.setSelectedComponent(editor);

        viewModel.selectResource(resourceFile);
    }

    private String getSimpleClassName(String fullName) {
        int lastDot = fullName.lastIndexOf('.');
        return lastDot == -1 ? fullName : fullName.substring(lastDot + 1);
    }

    private String getResourceKey(DexForgeResourceFile resourceFile) {
        String deobfuscatedName = resourceFile.getDeobfuscatedName();
        if (deobfuscatedName != null && !deobfuscatedName.isBlank()) {
            return deobfuscatedName;
        }
        return resourceFile.getOriginalName();
    }

    private String getFileName(String path) {
        int lastSlash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return lastSlash == -1 ? path : path.substring(lastSlash + 1);
    }

    private String syntaxForResource(String resourceName) {
        String lowerName = resourceName.toLowerCase();
        if (lowerName.endsWith(".xml")) {
            return SyntaxConstants.SYNTAX_STYLE_XML;
        }
        if (lowerName.endsWith(".json")) {
            return SyntaxConstants.SYNTAX_STYLE_JSON;
        }
        if (lowerName.endsWith(".html") || lowerName.endsWith(".htm")) {
            return SyntaxConstants.SYNTAX_STYLE_HTML;
        }
        if (lowerName.endsWith(".css")) {
            return SyntaxConstants.SYNTAX_STYLE_CSS;
        }
        if (lowerName.endsWith(".js")) {
            return SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT;
        }
        return SyntaxConstants.SYNTAX_STYLE_NONE;
    }

    public JTabbedPane getComponent() {
        return tabbedPane;
    }
}

package jadx.gui.ui.action;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dexforge.frida.FridaScriptGenerator;
import dexforge.frida.IFridaScriptGenerator;

import jadx.api.JavaClass;
import jadx.core.utils.StringUtils;
import jadx.core.utils.exceptions.JadxRuntimeException;
import jadx.gui.treemodel.JClass;
import jadx.gui.treemodel.JField;
import jadx.gui.treemodel.JMethod;
import jadx.gui.treemodel.JNode;
import jadx.gui.ui.codearea.CodeArea;
import jadx.gui.ui.dialog.MethodsDialog;
import jadx.gui.utils.NLS;
import jadx.gui.utils.UiUtils;

public final class FridaAction extends JNodeAction {
	private static final Logger LOG = LoggerFactory.getLogger(FridaAction.class);
	private static final long serialVersionUID = -3084073927621269039L;

	private final IFridaScriptGenerator scriptGenerator;

	public FridaAction(CodeArea codeArea) {
		super(ActionModel.FRIDA_COPY, codeArea);
		this.scriptGenerator = new FridaScriptGenerator();
	}

	@Override
	public void runAction(JNode node) {
		try {
			generateFridaSnippet(node);
		} catch (Exception e) {
			LOG.error("Failed to generate Frida code snippet", e);
			JOptionPane.showMessageDialog(getCodeArea().getMainWindow(), e.getLocalizedMessage(), NLS.str("error_dialog.title"),
					JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public boolean isActionEnabled(JNode node) {
		return node instanceof JMethod || node instanceof JClass || node instanceof JField;
	}

	private void generateFridaSnippet(JNode node) {
		String fridaSnippet;
		if (node instanceof JMethod) {
			JMethod jMth = (JMethod) node;
			String classSnippet = scriptGenerator.generateClassSnippet(jMth.getJParent().getCls());
			String methodSnippet = scriptGenerator.generateMethodSnippet(jMth.getJavaMethod(), jMth.getJParent().getCls());
			fridaSnippet = String.format("%s\n%s", classSnippet, methodSnippet);
			copySnipped(fridaSnippet);
		} else if (node instanceof JField) {
			JField jf = (JField) node;
			fridaSnippet = scriptGenerator.generateFieldSnippet(jf.getJavaField(), jf.getRootClass().getCls());
			copySnipped(fridaSnippet);
		} else if (node instanceof JClass) {
			SwingUtilities.invokeLater(() -> showMethodSelectionDialog((JClass) node));
		} else {
			throw new JadxRuntimeException("Unsupported node type: " + (node != null ? node.getClass() : "null"));
		}

	}

	private void copySnipped(String fridaSnippet) {
		if (!StringUtils.isEmpty(fridaSnippet)) {
			LOG.info("Frida snippet:\n{}", fridaSnippet);
			UiUtils.copyToClipboard(fridaSnippet);
		}
	}

	private void showMethodSelectionDialog(JClass jc) {
		JavaClass javaClass = jc.getCls();
		new MethodsDialog(getCodeArea().getMainWindow(), javaClass.getMethods(), (result) -> {
			String fridaSnippet = scriptGenerator.generateClassAllMethodSnippet(javaClass, result);
			copySnipped(fridaSnippet);
		});
	}
}

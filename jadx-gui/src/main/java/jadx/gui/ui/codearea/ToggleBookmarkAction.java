package jadx.gui.ui.codearea;

import java.awt.event.ActionEvent;

import javax.swing.text.BadLocationException;

import jadx.gui.treemodel.JClass;
import jadx.gui.treemodel.JNode;
import jadx.gui.ui.action.ActionModel;
import jadx.gui.ui.action.CodeAreaAction;

public class ToggleBookmarkAction extends CodeAreaAction {
	private static final long serialVersionUID = 1L;

	public ToggleBookmarkAction(CodeArea codeArea) {
		super(ActionModel.TOGGLE_BOOKMARK, codeArea);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JNode node = codeArea.getNode();
		if (node instanceof JClass) {
			int pos = codeArea.getCaretPosition();
			try {
				int line = codeArea.getLineOfOffset(pos) + 1; // 1-indexed
				codeArea.getMainWindow().toggleBookmark((JClass) node, line);
			} catch (BadLocationException ex) {
				// ignore
			}
		}
	}
}

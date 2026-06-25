package dexforge.gui.presentation.view.action;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.KeyStroke;

/**
 * Base class for all IDE actions.
 * Implements Command Pattern for Menu, Toolbar, and Shortcuts.
 */
public abstract class DexForgeAction extends AbstractAction {

    protected DexForgeAction(String name) {
        super(name);
    }

    protected DexForgeAction(String name, Icon icon) {
        super(name, icon);
    }

    public void setShortcut(KeyStroke keyStroke) {
        putValue(ACCELERATOR_KEY, keyStroke);
    }

    public void setDescription(String description) {
        putValue(SHORT_DESCRIPTION, description);
    }

    @Override
    public abstract void actionPerformed(ActionEvent e);
}

package dexforge.gui.presentation.view;

import java.awt.BorderLayout;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import dexforge.gui.presentation.viewmodel.MainViewModel;

public final class ClassTreePanel extends JPanel {
	private final MainViewModel viewModel;
	private final JList<String> classList;
	private final DefaultListModel<String> listModel;

	public ClassTreePanel(MainViewModel viewModel) {
		this.viewModel = viewModel;
		this.listModel = new DefaultListModel<>();
		this.classList = new JList<>(listModel);

		setLayout(new BorderLayout());
		add(new JScrollPane(classList), BorderLayout.CENTER);

		bindViewModel();
	}

	private void bindViewModel() {
		viewModel.onClassesLoaded(classes -> {
			listModel.clear();
			for (String cls : classes) {
				listModel.addElement(cls);
			}
		});

		classList.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				String selected = classList.getSelectedValue();
				if (selected != null) {
					viewModel.selectClass(selected);
				}
			}
		});
	}
}

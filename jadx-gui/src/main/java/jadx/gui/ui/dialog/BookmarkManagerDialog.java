package jadx.gui.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import jadx.gui.settings.data.Bookmark;
import jadx.gui.treemodel.JClass;
import jadx.gui.ui.MainWindow;
import jadx.gui.utils.NLS;
import jadx.gui.utils.UiUtils;

public class BookmarkManagerDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private final MainWindow mainWindow;
	private JTable table;
	private DefaultTableModel tableModel;

	private boolean loadingBookmarks = false;

	public BookmarkManagerDialog(MainWindow mainWindow) {
		super(mainWindow, NLS.str("bookmarks.title"), ModalityType.APPLICATION_MODAL);
		this.mainWindow = mainWindow;
		initUI();
		loadBookmarks();
		UiUtils.addEscapeShortCutToDispose(this);
	}

	private void initUI() {
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setSize(new Dimension(600, 450));
		setLocationRelativeTo(mainWindow);

		JPanel panel = new JPanel(new BorderLayout(10, 10));
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);

		// Table Model & JTable
		tableModel = new DefaultTableModel(new Object[]{
				NLS.str("bookmarks.column_class"),
				NLS.str("bookmarks.column_line"),
				NLS.str("bookmarks.column_desc")
		}, 0) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return column == 2;
			}
		};

		tableModel.addTableModelListener(e -> {
			if (loadingBookmarks) {
				return;
			}
			if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
				int row = e.getFirstRow();
				int column = e.getColumn();
				if (column == 2) {
					String newDesc = (String) tableModel.getValueAt(row, column);
					Bookmark bookmark = mainWindow.getProject().getBookmarks().get(row);
					if (newDesc != null && !newDesc.equals(bookmark.getDescription())) {
						mainWindow.getProject().removeBookmark(bookmark);
						bookmark.setDescription(newDesc);
						mainWindow.getProject().addBookmark(bookmark);
						SwingUtilities.invokeLater(this::loadBookmarks);
					}
				}
			}
		});

		table = new JTable(tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setAutoCreateRowSorter(true);

		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					jumpToSelectedBookmark();
				}
			}
		});

		JScrollPane scrollPane = new JScrollPane(table);
		panel.add(scrollPane, BorderLayout.CENTER);

		// Buttons
		JPanel buttonPanel = new JPanel(new BorderLayout(10, 10));

		JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		JButton jumpButton = new JButton(NLS.str("bookmarks.btn_jump"));
		jumpButton.addActionListener(e -> jumpToSelectedBookmark());
		actionPanel.add(jumpButton);

		JButton editButton = new JButton(NLS.str("bookmarks.btn_edit"));
		editButton.addActionListener(e -> editSelectedBookmark());
		actionPanel.add(editButton);

		JButton deleteButton = new JButton(NLS.str("bookmarks.btn_delete"));
		deleteButton.addActionListener(e -> deleteSelectedBookmark());
		actionPanel.add(deleteButton);

		JButton clearButton = new JButton(NLS.str("bookmarks.btn_clear"));
		clearButton.addActionListener(e -> clearAllBookmarks());
		actionPanel.add(clearButton);

		buttonPanel.add(actionPanel, BorderLayout.CENTER);

		JButton closeButton = new JButton(NLS.str("common_dialog.cancel"));
		closeButton.addActionListener(e -> dispose());
		buttonPanel.add(closeButton, BorderLayout.EAST);

		panel.add(buttonPanel, BorderLayout.SOUTH);
	}

	private void loadBookmarks() {
		loadingBookmarks = true;
		tableModel.setRowCount(0);
		List<Bookmark> bookmarks = mainWindow.getProject().getBookmarks();
		for (Bookmark bookmark : bookmarks) {
			String className = bookmark.getNodeRef() != null ? bookmark.getNodeRef().getDeclaringClass() : "Unknown";
			tableModel.addRow(new Object[]{
					className,
					bookmark.getLine(),
					bookmark.getDescription()
			});
		}
		loadingBookmarks = false;
	}

	private void jumpToSelectedBookmark() {
		int selectedRow = table.getSelectedRow();
		if (selectedRow == -1) {
			return;
		}
		int modelRow = table.convertRowIndexToModel(selectedRow);
		Bookmark bookmark = mainWindow.getProject().getBookmarks().get(modelRow);
		if (bookmark.getNodeRef() != null) {
			String className = bookmark.getNodeRef().getDeclaringClass();
			JClass jClass = mainWindow.searchClassByName(className);
			if (jClass != null) {
				dispose();
				mainWindow.getTabsController().codeJumpToLine(jClass, bookmark.getLine());
			} else {
				JOptionPane.showMessageDialog(this,
						"Class not found in project: " + className,
						"Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void editSelectedBookmark() {
		int selectedRow = table.getSelectedRow();
		if (selectedRow == -1) {
			return;
		}
		int modelRow = table.convertRowIndexToModel(selectedRow);
		Bookmark bookmark = mainWindow.getProject().getBookmarks().get(modelRow);

		String newDesc = JOptionPane.showInputDialog(this,
				NLS.str("bookmarks.prompt_desc"),
				bookmark.getDescription());

		if (newDesc != null) {
			mainWindow.getProject().removeBookmark(bookmark);
			bookmark.setDescription(newDesc);
			mainWindow.getProject().addBookmark(bookmark);
			loadBookmarks();
		}
	}

	private void deleteSelectedBookmark() {
		int selectedRow = table.getSelectedRow();
		if (selectedRow == -1) {
			return;
		}
		int modelRow = table.convertRowIndexToModel(selectedRow);
		Bookmark bookmark = mainWindow.getProject().getBookmarks().get(modelRow);
		mainWindow.getProject().removeBookmark(bookmark);
		refreshGutterIcons();
		loadBookmarks();
	}

	private void clearAllBookmarks() {
		int confirm = JOptionPane.showConfirmDialog(this,
				NLS.str("bookmarks.btn_clear") + "?",
				NLS.str("bookmarks.title"),
				JOptionPane.YES_NO_OPTION);
		if (confirm == JOptionPane.YES_OPTION) {
			mainWindow.getProject().clearBookmarks();
			refreshGutterIcons();
			loadBookmarks();
		}
	}

	private void refreshGutterIcons() {
		mainWindow.getTabbedPane().getTabs().forEach(v -> {
			if (v instanceof jadx.gui.ui.codearea.AbstractCodeContentPanel) {
				jadx.gui.ui.codearea.AbstractCodeArea area = ((jadx.gui.ui.codearea.AbstractCodeContentPanel) v).getCodeArea();
				if (area != null) {
					area.updateBookmarkIcons();
				}
				if (v instanceof jadx.gui.ui.codearea.ClassCodeContentPanel) {
					area = ((jadx.gui.ui.codearea.ClassCodeContentPanel) v).getSmaliCodeArea();
					if (area != null) {
						area.updateBookmarkIcons();
					}
				}
			}
		});
	}
}

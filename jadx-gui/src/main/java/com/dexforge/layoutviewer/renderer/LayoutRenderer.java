package com.dexforge.layoutviewer.renderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.OverlayLayout;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import com.dexforge.layoutviewer.model.AndroidViewNode;

public class LayoutRenderer {
	private static final Color PREVIEW_BACKGROUND = new Color(0xF4F6F8);
	private static final Color SELECTION_BORDER = new Color(0x2563EB);
	private static final String NODE_PROPERTY = "dexforge.viewNode";
	private static final String BASE_BORDER_PROPERTY = "dexforge.baseBorder";

	private final Consumer<AndroidViewNode> selectionListener;
	private final RenderStyleMapper styleMapper = new RenderStyleMapper();
	private JComponent selectedComponent;

	public LayoutRenderer() {
		this(null);
	}

	public LayoutRenderer(Consumer<AndroidViewNode> selectionListener) {
		this.selectionListener = selectionListener;
	}

	public JComponent render(AndroidViewNode root) {
		JPanel surface = new JPanel(new GridBagLayout());
		surface.setBackground(PREVIEW_BACKGROUND);
		surface.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.fill = GridBagConstraints.BOTH;
		surface.add(renderSelectableNode(root), constraints);
		return surface;
	}

	private JComponent renderNode(AndroidViewNode node) {
		if (isGone(node)) {
			return hiddenPlaceholder(node);
		}
		String tag = simpleTag(node.getTag());
		switch (tag) {
			case "LinearLayout":
				return renderLinearLayout(node);
			case "FrameLayout":
				return renderFrameLayout(node);
			case "ConstraintLayout":
			case "RelativeLayout":
			case "androidx.cardview.widget.CardView":
			case "CardView":
				return renderContainer(node);
			case "ScrollView":
				return renderScrollView(node, false);
			case "HorizontalScrollView":
				return renderScrollView(node, true);
			case "TextView":
				return styleLeaf(node, new JLabel(text(node, viewIdLabel(node, "TextView"))));
			case "Button":
				return styleLeaf(node, new JButton(text(node, "Button")));
			case "EditText":
				JTextField field = new JTextField(text(node, ""));
				field.setColumns(16);
				return styleLeaf(node, field);
			case "ImageView":
				JLabel image = new JLabel(imageLabel(node), SwingConstants.CENTER);
				image.setPreferredSize(new Dimension(120, 80));
				image.setOpaque(true);
				image.setBackground(new Color(0xE2E8F0));
				return styleLeaf(node, image);
			case "RecyclerView":
			case "ListView":
				return renderListPlaceholder(node);
			default:
				if (!node.getChildren().isEmpty()) {
					return renderContainer(node);
				}
				return styleLeaf(node, new JLabel(tag));
		}
	}

	private JComponent renderSelectableNode(AndroidViewNode node) {
		JComponent component = renderNode(node);
		component.putClientProperty(NODE_PROPERTY, node);
		installSelectionHandler(component, node);
		return component;
	}

	private JComponent renderLinearLayout(AndroidViewNode node) {
		JPanel panel = new JPanel();
		boolean horizontal = "horizontal".equalsIgnoreCase(node.getAttribute("android:orientation"));
		panel.setLayout(new BoxLayout(panel, horizontal ? BoxLayout.X_AXIS : BoxLayout.Y_AXIS));
		for (AndroidViewNode child : node.getChildren()) {
			if (isGone(child)) {
				continue;
			}
			JComponent childComponent = applyMargins(child, renderSelectableNode(child));
			childComponent.setAlignmentX(Component.LEFT_ALIGNMENT);
			panel.add(childComponent);
		}
		return styleContainer(node, panel);
	}

	private JComponent renderContainer(AndroidViewNode node) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		for (AndroidViewNode child : node.getChildren()) {
			if (isGone(child)) {
				continue;
			}
			JComponent childComponent = applyMargins(child, renderSelectableNode(child));
			childComponent.setAlignmentX(Component.LEFT_ALIGNMENT);
			panel.add(childComponent);
		}
		if (node.getChildren().isEmpty()) {
			panel.add(new JLabel(simpleTag(node.getTag()), SwingConstants.CENTER));
		}
		return styleContainer(node, panel);
	}

	private JComponent renderFrameLayout(AndroidViewNode node) {
		JPanel panel = new JPanel();
		panel.setLayout(new OverlayLayout(panel));
		for (AndroidViewNode child : node.getChildren()) {
			if (isGone(child)) {
				continue;
			}
			JComponent childComponent = applyMargins(child, renderSelectableNode(child));
			childComponent.setAlignmentX(styleMapper.alignment(child.getAttribute("android:layout_gravity"), true));
			childComponent.setAlignmentY(styleMapper.alignment(child.getAttribute("android:layout_gravity"), false));
			panel.add(childComponent);
		}
		if (node.getChildren().isEmpty()) {
			panel.add(new JLabel(simpleTag(node.getTag()), SwingConstants.CENTER), JLayeredPane.DEFAULT_LAYER);
		}
		return styleContainer(node, panel);
	}

	private JComponent renderScrollView(AndroidViewNode node, boolean horizontal) {
		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, horizontal ? BoxLayout.X_AXIS : BoxLayout.Y_AXIS));
		for (AndroidViewNode child : node.getChildren()) {
			if (isGone(child)) {
				continue;
			}
			content.add(applyMargins(child, renderSelectableNode(child)));
		}
		if (node.getChildren().isEmpty()) {
			content.add(new JLabel(simpleTag(node.getTag()), SwingConstants.CENTER));
		}
		JScrollPane scrollPane = new JScrollPane(styleContainer(node, content));
		scrollPane.setHorizontalScrollBarPolicy(horizontal
				? JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
				: JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(horizontal
				? JScrollPane.VERTICAL_SCROLLBAR_NEVER
				: JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setPreferredSize(styleMapper.preferredSize(node, new Dimension(horizontal ? 360 : 280, horizontal ? 120 : 360)));
		return scrollPane;
	}

	private JComponent renderListPlaceholder(AndroidViewNode node) {
		JPanel list = new JPanel();
		list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
		for (int i = 1; i <= 3; i++) {
			JLabel row = new JLabel("Recycler item " + i);
			row.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
			row.setAlignmentX(Component.LEFT_ALIGNMENT);
			list.add(row);
		}
		return styleContainer(node, list);
	}

	private JComponent styleContainer(AndroidViewNode node, JComponent component) {
		component.setOpaque(true);
		component.setBackground(styleMapper.background(node, Color.WHITE));
		Insets padding = styleMapper.padding(node);
		Border baseBorder = BorderFactory.createCompoundBorder(styleMapper.border(node),
				BorderFactory.createEmptyBorder(padding.top, padding.left, padding.bottom, padding.right));
		component.setBorder(baseBorder);
		component.putClientProperty(BASE_BORDER_PROPERTY, baseBorder);
		component.setPreferredSize(styleMapper.preferredSize(node, component.getPreferredSize()));
		return component;
	}

	private JComponent styleLeaf(AndroidViewNode node, JComponent component) {
		if (component instanceof JLabel) {
			((JLabel) component).setForeground(styleMapper.textColor(node, Color.DARK_GRAY));
		}
		if (component instanceof JLabel || component instanceof JButton || component instanceof JTextField) {
			component.setFont(styleMapper.font(node, component.getFont()));
			component.setForeground(styleMapper.textColor(node, component.getForeground()));
		}
		if ("invisible".equalsIgnoreCase(node.getAttribute("android:visibility"))) {
			component.setEnabled(false);
		}
		Border baseBorder = BorderFactory.createCompoundBorder(styleMapper.border(node), BorderFactory.createEmptyBorder(6, 8, 6, 8));
		component.setBorder(baseBorder);
		component.putClientProperty(BASE_BORDER_PROPERTY, baseBorder);
		component.setMaximumSize(new Dimension(Integer.MAX_VALUE, component.getPreferredSize().height + 12));
		component.setPreferredSize(styleMapper.preferredSize(node, component.getPreferredSize()));
		return component;
	}

	private JComponent applyMargins(AndroidViewNode node, JComponent component) {
		Insets margin = styleMapper.margin(node);
		if (margin.top == 0 && margin.left == 0 && margin.bottom == 0 && margin.right == 0) {
			return component;
		}
		JPanel wrapper = new JPanel(new GridBagLayout());
		wrapper.setOpaque(false);
		wrapper.setBorder(BorderFactory.createEmptyBorder(margin.top, margin.left, margin.bottom, margin.right));
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.fill = GridBagConstraints.BOTH;
		wrapper.add(component, constraints);
		wrapper.setAlignmentX(component.getAlignmentX());
		wrapper.setAlignmentY(component.getAlignmentY());
		return wrapper;
	}

	private void installSelectionHandler(JComponent component, AndroidViewNode node) {
		if (selectionListener == null) {
			return;
		}
		component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		component.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				event.consume();
				highlight(component);
				selectionListener.accept(node);
			}
		});
	}

	private void highlight(JComponent component) {
		clearSelection();
		selectedComponent = component;
		Border current = component.getBorder();
		component.putClientProperty(BASE_BORDER_PROPERTY, current);
		component.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(SELECTION_BORDER, 2),
				current));
		component.revalidate();
		component.repaint();
	}

	private void clearSelection() {
		if (selectedComponent == null) {
			return;
		}
		Object baseBorder = selectedComponent.getClientProperty(BASE_BORDER_PROPERTY);
		if (baseBorder instanceof Border) {
			selectedComponent.setBorder((Border) baseBorder);
		}
		selectedComponent.revalidate();
		selectedComponent.repaint();
		selectedComponent = null;
	}

	private String text(AndroidViewNode node, String fallback) {
		String text = node.getAttribute("android:text");
		if (text == null || text.isBlank()) {
			text = node.getAttribute("android:hint");
		}
		if (text == null || text.isBlank()) {
			return fallback;
		}
		return text;
	}

	private JComponent hiddenPlaceholder(AndroidViewNode node) {
		JLabel label = new JLabel(viewIdLabel(node, simpleTag(node.getTag())) + " (gone)", SwingConstants.CENTER);
		label.setEnabled(false);
		label.setPreferredSize(new Dimension(1, 1));
		return label;
	}

	private boolean isGone(AndroidViewNode node) {
		return "gone".equalsIgnoreCase(node.getAttribute("android:visibility"));
	}

	private String viewIdLabel(AndroidViewNode node, String fallback) {
		String id = node.getAttribute("android:id");
		if (id == null || id.isBlank()) {
			return fallback;
		}
		int slash = id.lastIndexOf('/');
		return slash == -1 ? id : id.substring(slash + 1);
	}

	private String imageLabel(AndroidViewNode node) {
		String source = node.getAttribute("android:src");
		if (source == null) {
			source = node.getAttribute("app:srcCompat");
		}
		return source == null ? "ImageView" : source;
	}

	private String simpleTag(String tag) {
		int dot = tag.lastIndexOf('.');
		return dot == -1 ? tag : tag.substring(dot + 1);
	}

}

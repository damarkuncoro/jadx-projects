package com.dexforge.layoutviewer.renderer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import com.dexforge.layoutviewer.model.AndroidViewNode;
import com.dexforge.layoutviewer.resolver.ResourceResolver;

public class RenderStyleMapper {
	private static final Color VIEW_BORDER = new Color(0xCBD5E1);

	public Dimension preferredSize(AndroidViewNode node, Dimension fallback) {
		int width = dimension(node.getAttribute("android:layout_width"), fallback.width);
		int height = dimension(node.getAttribute("android:layout_height"), fallback.height);
		if (!node.getChildren().isEmpty()) {
			width = Math.max(width, 160);
			height = Math.max(height, 48);
		}
		return new Dimension(Math.max(width, 40), Math.max(height, 28));
	}

	public Insets padding(AndroidViewNode node) {
		int drawableLeft = dp(node.getAttribute(ResourceResolver.ATTR_DRAWABLE_PADDING_LEFT), -1);
		int drawableTop = dp(node.getAttribute(ResourceResolver.ATTR_DRAWABLE_PADDING_TOP), -1);
		int drawableRight = dp(node.getAttribute(ResourceResolver.ATTR_DRAWABLE_PADDING_RIGHT), -1);
		int drawableBottom = dp(node.getAttribute(ResourceResolver.ATTR_DRAWABLE_PADDING_BOTTOM), -1);
		int all = dp(node.getAttribute("android:padding"), 8);
		int horizontal = dp(node.getAttribute("android:paddingHorizontal"), all);
		int vertical = dp(node.getAttribute("android:paddingVertical"), all);
		return new Insets(
				firstPositive(dp(node.getAttribute("android:paddingTop"), vertical), drawableTop),
				firstPositive(dp(firstNonNull(node.getAttribute("android:paddingLeft"), node.getAttribute("android:paddingStart")), horizontal), drawableLeft),
				firstPositive(dp(node.getAttribute("android:paddingBottom"), vertical), drawableBottom),
				firstPositive(dp(firstNonNull(node.getAttribute("android:paddingRight"), node.getAttribute("android:paddingEnd")), horizontal), drawableRight));
	}

	public Insets margin(AndroidViewNode node) {
		int all = dp(node.getAttribute("android:layout_margin"), 0);
		int horizontal = dp(node.getAttribute("android:layout_marginHorizontal"), all);
		int vertical = dp(node.getAttribute("android:layout_marginVertical"), all);
		return new Insets(
				dp(node.getAttribute("android:layout_marginTop"), vertical),
				dp(firstNonNull(node.getAttribute("android:layout_marginLeft"), node.getAttribute("android:layout_marginStart")), horizontal),
				dp(node.getAttribute("android:layout_marginBottom"), vertical),
				dp(firstNonNull(node.getAttribute("android:layout_marginRight"), node.getAttribute("android:layout_marginEnd")), horizontal));
	}

	public Border border(AndroidViewNode node) {
		Color strokeColor = color(node.getAttribute(ResourceResolver.ATTR_DRAWABLE_STROKE_COLOR), VIEW_BORDER);
		int strokeWidth = dp(node.getAttribute(ResourceResolver.ATTR_DRAWABLE_STROKE_WIDTH), 1);
		int radius = dp(node.getAttribute(ResourceResolver.ATTR_DRAWABLE_CORNER_RADIUS), 0);
		if (radius > 0) {
			return new LineBorder(strokeColor, Math.max(1, strokeWidth), true);
		}
		return BorderFactory.createLineBorder(strokeColor, Math.max(1, strokeWidth));
	}

	public Color background(AndroidViewNode node, Color fallback) {
		return color(backgroundColor(node), fallback);
	}

	public Color textColor(AndroidViewNode node, Color fallback) {
		return color(node.getAttribute("android:textColor"), fallback);
	}

	public Font font(AndroidViewNode node, Font fallback) {
		int size = dp(node.getAttribute("android:textSize"), fallback.getSize());
		int style = Font.PLAIN;
		String textStyle = node.getAttribute("android:textStyle");
		if (textStyle != null) {
			if (textStyle.contains("bold")) {
				style |= Font.BOLD;
			}
			if (textStyle.contains("italic")) {
				style |= Font.ITALIC;
			}
		}
		return fallback.deriveFont(style, size);
	}

	public float alignment(String gravity, boolean horizontal) {
		if (gravity == null) {
			return 0.5f;
		}
		String normalized = gravity.toLowerCase(Locale.ROOT);
		if (horizontal) {
			if (normalized.contains("left") || normalized.contains("start")) {
				return 0.0f;
			}
			if (normalized.contains("right") || normalized.contains("end")) {
				return 1.0f;
			}
		} else {
			if (normalized.contains("top")) {
				return 0.0f;
			}
			if (normalized.contains("bottom")) {
				return 1.0f;
			}
		}
		return 0.5f;
	}

	private int dimension(String value, int fallback) {
		if (value == null || value.isBlank() || "wrap_content".equals(value)) {
			return fallback;
		}
		if ("match_parent".equals(value) || "fill_parent".equals(value)) {
			return Math.max(fallback, 320);
		}
		return dp(value, fallback);
	}

	private int dp(String value, int fallback) {
		if (value == null) {
			return fallback;
		}
		String normalized = value.toLowerCase(Locale.ROOT).replace("dp", "").replace("sp", "").trim();
		try {
			return Math.max(0, Math.round(Float.parseFloat(normalized)));
		} catch (NumberFormatException e) {
			return fallback;
		}
	}

	private Color color(String value, Color fallback) {
		if (value == null || !value.startsWith("#")) {
			return fallback;
		}
		try {
			if (value.length() == 9) {
				return new Color((int) Long.parseLong(value.substring(1), 16), true);
			}
			return Color.decode(value);
		} catch (NumberFormatException e) {
			return fallback;
		}
	}

	private String backgroundColor(AndroidViewNode node) {
		String drawableBackground = node.getAttribute(ResourceResolver.ATTR_DRAWABLE_BACKGROUND);
		if (drawableBackground != null) {
			return drawableBackground;
		}
		return node.getAttribute("android:background");
	}

	private String firstNonNull(String first, String second) {
		return first != null ? first : second;
	}

	private int firstPositive(int first, int second) {
		return second >= 0 ? second : first;
	}
}

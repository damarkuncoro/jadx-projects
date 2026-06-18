package com.dexforge.layoutviewer.resolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jadx.api.ResourceFile;

import com.dexforge.layoutviewer.model.AndroidResource;
import com.dexforge.layoutviewer.model.AndroidViewNode;
import com.dexforge.layoutviewer.model.RenderStyle;

public class ResourceResolver {
	private static final String STYLE_PREFIX = "@style/";
	public static final String ATTR_DRAWABLE_BACKGROUND = "dexforge:drawableBackground";
	public static final String ATTR_DRAWABLE_STROKE_COLOR = "dexforge:drawableStrokeColor";
	public static final String ATTR_DRAWABLE_STROKE_WIDTH = "dexforge:drawableStrokeWidth";
	public static final String ATTR_DRAWABLE_CORNER_RADIUS = "dexforge:drawableCornerRadius";
	public static final String ATTR_DRAWABLE_PADDING_LEFT = "dexforge:drawablePaddingLeft";
	public static final String ATTR_DRAWABLE_PADDING_TOP = "dexforge:drawablePaddingTop";
	public static final String ATTR_DRAWABLE_PADDING_RIGHT = "dexforge:drawablePaddingRight";
	public static final String ATTR_DRAWABLE_PADDING_BOTTOM = "dexforge:drawablePaddingBottom";

	private final Map<String, String> values = new LinkedHashMap<>();
	private final Map<String, Map<String, String>> styles = new LinkedHashMap<>();
	private final Map<String, String> styleParents = new LinkedHashMap<>();
	private final Map<String, RenderStyle> drawables = new LinkedHashMap<>();

	public static ResourceResolver fromResources(List<ResourceFile> resources) {
		return new AndroidResourceLoader().load(resources);
	}

	public void putValue(String type, String name, String value) {
		if (type != null && name != null && value != null) {
			values.put("@" + type + "/" + name, value.trim());
		}
	}

	public void putStyle(String name, Map<String, String> attrs) {
		styles.put(name, new LinkedHashMap<>(attrs));
	}

	public void putStyle(String name, String parent, Map<String, String> attrs) {
		putStyle(name, attrs);
		if (parent != null && !parent.isBlank()) {
			styleParents.put(name, normalizeStyleName(parent));
		}
	}

	public void putDrawable(String path, RenderStyle style) {
		if (style == null || !style.hasAnyValue()) {
			return;
		}
		String ref = ResourceRefs.fromPath(path);
		if (ref != null) {
			drawables.put(ref, style);
		}
	}

	public String resolveValue(String value) {
		if (value == null) {
			return null;
		}
		if (value.startsWith("@android:")) {
			return value.substring("@android:".length());
		}
		if (value.startsWith("?attr/") || value.startsWith("?android:attr/")) {
			return value;
		}
		String resolved = values.get(value);
		return resolved != null ? resolved : value;
	}

	public void resolveTree(AndroidViewNode root) {
		applyStyle(root);
		for (Map.Entry<String, String> entry : root.getAttributes().entrySet()) {
			root.putResolvedAttribute(entry.getKey(), resolveValue(entry.getValue()));
		}
		applyDrawable(root);
		for (AndroidViewNode child : root.getChildren()) {
			resolveTree(child);
		}
	}

	public List<AndroidResource> getResources() {
		List<AndroidResource> resources = new ArrayList<>();
		for (Map.Entry<String, String> entry : values.entrySet()) {
			String key = entry.getKey();
			int slash = key.indexOf('/');
			if (slash > 1) {
				resources.add(new AndroidResource(key.substring(1, slash), key.substring(slash + 1), entry.getValue()));
			}
		}
		return Collections.unmodifiableList(resources);
	}

	private void applyStyle(AndroidViewNode node) {
		String styleRef = node.getAttributes().get("style");
		if (styleRef == null || !styleRef.startsWith(STYLE_PREFIX)) {
			return;
		}
		Map<String, String> style = collectStyle(styleRef.substring(STYLE_PREFIX.length()));
		if (style == null) {
			return;
		}
		for (Map.Entry<String, String> entry : style.entrySet()) {
			if (!node.getAttributes().containsKey(entry.getKey())) {
				node.putResolvedAttribute(entry.getKey(), resolveValue(entry.getValue()));
			}
		}
	}

	private void applyDrawable(AndroidViewNode node) {
		String background = node.getAttributes().get("android:background");
		if (background == null) {
			background = node.getResolvedAttributes().get("android:background");
		}
		if (background == null) {
			return;
		}
		RenderStyle style = drawables.get(ResourceRefs.drawableRef(background));
		if (style == null) {
			return;
		}
		putStyleAttr(node, ATTR_DRAWABLE_BACKGROUND, resolveValue(style.getBackgroundColor()));
		putStyleAttr(node, ATTR_DRAWABLE_STROKE_COLOR, resolveValue(style.getStrokeColor()));
		putStyleAttr(node, ATTR_DRAWABLE_STROKE_WIDTH, resolveValue(style.getStrokeWidth()));
		putStyleAttr(node, ATTR_DRAWABLE_CORNER_RADIUS, resolveValue(style.getCornerRadius()));
		putStyleAttr(node, ATTR_DRAWABLE_PADDING_LEFT, resolveValue(style.getPaddingLeft()));
		putStyleAttr(node, ATTR_DRAWABLE_PADDING_TOP, resolveValue(style.getPaddingTop()));
		putStyleAttr(node, ATTR_DRAWABLE_PADDING_RIGHT, resolveValue(style.getPaddingRight()));
		putStyleAttr(node, ATTR_DRAWABLE_PADDING_BOTTOM, resolveValue(style.getPaddingBottom()));
	}

	private void putStyleAttr(AndroidViewNode node, String attr, String value) {
		if (value != null && !value.isBlank()) {
			node.putResolvedAttribute(attr, value);
		}
	}

	private Map<String, String> collectStyle(String name) {
		return collectStyle(name, new HashSet<>());
	}

	private Map<String, String> collectStyle(String name, Set<String> visited) {
		if (!visited.add(name)) {
			return null;
		}
		Map<String, String> attrs = styles.get(name);
		if (attrs == null) {
			return null;
		}
		Map<String, String> merged = new LinkedHashMap<>();
		String parent = styleParents.get(name);
		if (parent != null) {
			Map<String, String> parentAttrs = collectStyle(parent, visited);
			if (parentAttrs != null) {
				merged.putAll(parentAttrs);
			}
		}
		merged.putAll(attrs);
		mergeNestedStyle(merged, "android:textAppearance", visited);
		mergeNestedStyle(merged, "textAppearance", visited);
		mergeNestedStyle(merged, "android:buttonStyle", visited);
		mergeNestedStyle(merged, "buttonStyle", visited);
		return merged;
	}

	private void mergeNestedStyle(Map<String, String> merged, String attr, Set<String> visited) {
		String nestedStyle = merged.get(attr);
		if (nestedStyle == null || !nestedStyle.startsWith(STYLE_PREFIX)) {
			return;
		}
		Map<String, String> nestedAttrs = collectStyle(normalizeStyleName(nestedStyle), new HashSet<>(visited));
		if (nestedAttrs == null) {
			return;
		}
		for (Map.Entry<String, String> entry : nestedAttrs.entrySet()) {
			merged.putIfAbsent(entry.getKey(), entry.getValue());
		}
	}

	private static String normalizeStyleName(String name) {
		if (name.startsWith(STYLE_PREFIX)) {
			return name.substring(STYLE_PREFIX.length());
		}
		if (name.startsWith("@android:style/")) {
			return name.substring("@android:style/".length());
		}
		return name;
	}
}

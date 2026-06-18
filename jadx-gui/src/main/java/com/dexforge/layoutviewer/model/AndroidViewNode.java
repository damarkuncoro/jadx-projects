package com.dexforge.layoutviewer.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AndroidViewNode {
	private final String tag;
	private final Map<String, String> attributes = new LinkedHashMap<>();
	private final Map<String, String> resolvedAttributes = new LinkedHashMap<>();
	private final List<AndroidViewNode> children = new ArrayList<>();

	public AndroidViewNode(String tag) {
		this.tag = tag;
	}

	public String getTag() {
		return tag;
	}

	public Map<String, String> getAttributes() {
		return Collections.unmodifiableMap(attributes);
	}

	public Map<String, String> getResolvedAttributes() {
		return Collections.unmodifiableMap(resolvedAttributes);
	}

	public List<AndroidViewNode> getChildren() {
		return Collections.unmodifiableList(children);
	}

	public void putAttribute(String name, String value) {
		attributes.put(name, value);
	}

	public void putResolvedAttribute(String name, String value) {
		resolvedAttributes.put(name, value);
	}

	public String getAttribute(String name) {
		String value = resolvedAttributes.get(name);
		if (value != null) {
			return value;
		}
		return attributes.get(name);
	}

	public void addChild(AndroidViewNode child) {
		children.add(child);
	}

	@Override
	public String toString() {
		return tag;
	}
}

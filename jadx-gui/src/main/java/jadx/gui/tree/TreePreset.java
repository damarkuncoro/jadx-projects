package jadx.gui.tree;

import java.util.ArrayList;
import java.util.List;

public class TreePreset {
	private String name;
	private List<String> expandedPaths;

	public TreePreset() {
		// For deserialization
		this.name = "";
		this.expandedPaths = new ArrayList<>();
	}

	public TreePreset(String name, List<String> expandedPaths) {
		this.name = name;
		this.expandedPaths = expandedPaths;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getExpandedPaths() {
		return expandedPaths;
	}

	public void setExpandedPaths(List<String> expandedPaths) {
		this.expandedPaths = expandedPaths;
	}
}

package jadx.gui.settings.data;

import org.jetbrains.annotations.NotNull;

import jadx.api.data.IJavaNodeRef;
import jadx.api.data.impl.JadxNodeRef;

public class Bookmark {
	private int line;
	private String description;
	private IJavaNodeRef nodeRef;
	private long timestamp;

	public Bookmark() {
		this.timestamp = System.currentTimeMillis();
	}

	public Bookmark(@NotNull IJavaNodeRef nodeRef, int line, String description) {
		this.nodeRef = nodeRef;
		this.line = line;
		this.description = description;
		this.timestamp = System.currentTimeMillis();
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public IJavaNodeRef getNodeRef() {
		return nodeRef;
	}

	public void setNodeRef(IJavaNodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return "Bookmark{" +
				"line=" + line +
				", description='" + description + '\'' +
				", nodeRef=" + nodeRef +
				'}';
	}
}

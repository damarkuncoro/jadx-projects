package dexforge.core.parser.arsc.model;

import java.util.ArrayList;
import java.util.List;

public final class ArscType {
	private final int id;
	private final String name;
	private final List<ArscEntry> entries = new ArrayList<>();

	public ArscType(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public List<ArscEntry> getEntries() {
		return entries;
	}
}

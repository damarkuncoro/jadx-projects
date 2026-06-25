package dexforge.core.parser.arsc.model;

import java.util.ArrayList;
import java.util.List;

public final class ArscPackage {
	private final int id;
	private final String name;
	private final List<ArscType> types = new ArrayList<>();

	public ArscPackage(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public List<ArscType> getTypes() {
		return types;
	}
}

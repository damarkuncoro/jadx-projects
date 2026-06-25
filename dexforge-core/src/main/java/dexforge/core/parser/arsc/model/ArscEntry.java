package dexforge.core.parser.arsc.model;

public final class ArscEntry {
	private final int id;
	private final String name;
	private final ArscResourceValue value;

	public ArscEntry(int id, String name, ArscResourceValue value) {
		this.id = id;
		this.name = name;
		this.value = value;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public ArscResourceValue getValue() {
		return value;
	}
}

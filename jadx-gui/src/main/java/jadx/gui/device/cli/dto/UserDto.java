package jadx.gui.device.cli.dto;

public final class UserDto {
	private final int id;
	private final String name;

	public UserDto(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}

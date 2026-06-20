package dexforge.application.dto;

import java.util.Objects;

/**
 * DTO: Request untuk List Packages use case.
 */
public class ListPackagesRequest {
	private final String serial;
	private final int userId;
	private final String filter;

	public ListPackagesRequest(String serial, int userId, String filter) {
		this.serial = Objects.requireNonNull(serial);
		this.userId = userId;
		this.filter = filter != null ? filter : "user";
	}

	public String getSerial() {
		return serial;
	}

	public int getUserId() {
		return userId;
	}

	public String getFilter() {
		return filter;
	}
}

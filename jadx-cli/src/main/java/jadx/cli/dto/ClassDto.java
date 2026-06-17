package jadx.cli.dto;

public class ClassDto {
	private String fullName;
	private String shortName;
	private String alias;
	private String packageName;

	public ClassDto(String fullName, String shortName, String alias, String packageName) {
		this.fullName = fullName;
		this.shortName = shortName;
		this.alias = alias;
		this.packageName = packageName;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
}

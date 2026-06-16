package jadx.gui.ads;

import java.util.List;

public class AdNetwork {
	private String name;
	private String category;
	private List<String> packagePrefixes;

	public AdNetwork(String name, String category, List<String> packagePrefixes) {
		this.name = name;
		this.category = category;
		this.packagePrefixes = packagePrefixes;
	}

	public String getName() {
		return name;
	}

	public String getCategory() {
		return category;
	}

	public List<String> getPackagePrefixes() {
		return packagePrefixes;
	}

	public boolean matchesPackage(String packageName) {
		for (String prefix : packagePrefixes) {
			if (packageName.startsWith(prefix)) {
				return true;
			}
		}
		return false;
	}
}

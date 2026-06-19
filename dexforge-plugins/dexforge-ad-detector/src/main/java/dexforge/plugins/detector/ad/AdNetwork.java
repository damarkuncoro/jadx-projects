package dexforge.plugins.detector.ad;

import java.util.List;

public class AdNetwork {
	private String name;
	private String category;
	private List<String> packagePrefixes;
	private String fridaTemplate;

	public AdNetwork(String name, String category, List<String> packagePrefixes) {
		this(name, category, packagePrefixes, null);
	}

	public AdNetwork(String name, String category, List<String> packagePrefixes, String fridaTemplate) {
		this.name = name;
		this.category = category;
		this.packagePrefixes = packagePrefixes;
		this.fridaTemplate = fridaTemplate;
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

	public String getFridaTemplate() {
		return fridaTemplate;
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

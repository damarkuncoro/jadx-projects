package dexforge.plugins.detector.ad;

import java.util.ArrayList;
import java.util.List;

public class AdFinding {
	private AdNetwork network;
	private List<String> foundPackages;
	private List<String> foundClasses;

	public AdFinding(AdNetwork network) {
		this.network = network;
		this.foundPackages = new ArrayList<>();
		this.foundClasses = new ArrayList<>();
	}

	public AdNetwork getNetwork() {
		return network;
	}

	public void addPackage(String pkg) {
		if (!foundPackages.contains(pkg)) {
			foundPackages.add(pkg);
		}
	}

	public void addClass(String cls) {
		if (!foundClasses.contains(cls)) {
			foundClasses.add(cls);
		}
	}

	public List<String> getFoundPackages() {
		return foundPackages;
	}

	public List<String> getFoundClasses() {
		return foundClasses;
	}

	public boolean isEmpty() {
		return foundPackages.isEmpty() && foundClasses.isEmpty();
	}
}

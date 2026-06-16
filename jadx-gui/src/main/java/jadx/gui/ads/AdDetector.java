package jadx.gui.ads;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jadx.api.JadxDecompiler;
import jadx.api.JavaClass;

public class AdDetector {

	public static List<AdFinding> detectAds(JadxDecompiler decompiler) {
		List<AdFinding> findings = new ArrayList<>();
		for (AdNetwork network : AdNetwork.values()) {
			AdFinding finding = new AdFinding(network);
			findings.add(finding);
		}

		// Check classes (and extract packages from class names)
		Set<String> foundPackages = new HashSet<>();
		for (JavaClass cls : decompiler.getClasses()) {
			checkClass(cls, findings, foundPackages);
		}

		// Remove empty findings
		List<AdFinding> result = new ArrayList<>();
		for (AdFinding finding : findings) {
			if (!finding.isEmpty()) {
				result.add(finding);
			}
		}
		return result;
	}

	private static void checkClass(JavaClass cls, List<AdFinding> findings, Set<String> foundPackages) {
		String clsName = cls.getFullName();
		// Extract package name (everything before last dot)
		int lastDotIndex = clsName.lastIndexOf('.');
		String pkgName = (lastDotIndex > 0) ? clsName.substring(0, lastDotIndex) : "";

		for (AdFinding finding : findings) {
			for (String prefix : finding.getNetwork().getPackagePrefixes()) {
				if (clsName.startsWith(prefix)) {
					// Add package only once
					if (!foundPackages.contains(pkgName)) {
						finding.addPackage(pkgName);
						foundPackages.add(pkgName);
					}
					finding.addClass(clsName);
					break;
				}
			}
		}
	}
}

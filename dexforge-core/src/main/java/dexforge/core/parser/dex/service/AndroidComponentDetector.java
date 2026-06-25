package dexforge.core.parser.dex.service;

import java.util.ArrayList;
import java.util.List;
import dexforge.core.parser.dex.model.DexClass;

/**
 * Detects Android components (Activities, Services, etc.) by analyzing class hierarchy.
 */
public final class AndroidComponentDetector {
	private final DexProject project;

	public AndroidComponentDetector(DexProject project) {
		this.project = project;
	}

	public List<DexClass> findActivities() {
		return findSubclassesOf("Landroid/app/Activity;");
	}

	public List<DexClass> findServices() {
		return findSubclassesOf("Landroid/app/Service;");
	}

	public List<DexClass> findReceivers() {
		return findSubclassesOf("Landroid/content/BroadcastReceiver;");
	}

	public List<DexClass> findProviders() {
		return findSubclassesOf("Landroid/content/ContentProvider;");
	}

	public List<DexClass> findApplications() {
		return findSubclassesOf("Landroid/app/Application;");
	}

	private List<DexClass> findSubclassesOf(String superClassName) {
		List<DexClass> results = new ArrayList<>();
		for (DexClass clazz : project.getAllClasses()) {
			if (isSubclassOf(clazz, superClassName)) {
				results.add(clazz);
			}
		}
		return results;
	}

	private boolean isSubclassOf(DexClass clazz, String targetSuper) {
		String currentSuper = clazz.getSuperclass();
		while (currentSuper != null) {
			if (currentSuper.equals(targetSuper)) {
				return true;
			}
			// Find the DexClass for the current super to continue up the chain
			DexClass superClazz = project.findClass(currentSuper);
			if (superClazz == null) {
				break; // Superclass not in this DEX project
			}
			currentSuper = superClazz.getSuperclass();
		}
		return false;
	}
}

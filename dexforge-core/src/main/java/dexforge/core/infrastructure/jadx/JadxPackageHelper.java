package dexforge.core.infrastructure.jadx;

import java.util.List;

import jadx.api.JavaPackage;

/**
 * Internal helper to bridge JADX Package operations without exposing them to public API.
 */
public final class JadxPackageHelper {
	private JadxPackageHelper() {
	}

	public static String getName(Object pkg) {
		return ((JavaPackage) pkg).getName();
	}

	public static String getFullName(Object pkg) {
		return ((JavaPackage) pkg).getFullName();
	}

	public static String getRawName(Object pkg) {
		return ((JavaPackage) pkg).getRawName();
	}

	public static String getRawFullName(Object pkg) {
		return ((JavaPackage) pkg).getRawFullName();
	}

	public static List<?> getSubPackages(Object pkg) {
		return ((JavaPackage) pkg).getSubPackages();
	}

	public static List<?> getClasses(Object pkg) {
		return ((JavaPackage) pkg).getClasses();
	}

	public static List<?> getClassesNoDup(Object pkg) {
		return ((JavaPackage) pkg).getClassesNoDup();
	}

	public static boolean isRoot(Object pkg) {
		return ((JavaPackage) pkg).isRoot();
	}

	public static boolean isLeaf(Object pkg) {
		return ((JavaPackage) pkg).isLeaf();
	}

	public static boolean isDefault(Object pkg) {
		return ((JavaPackage) pkg).isDefault();
	}

	public static void rename(Object pkg, String alias) {
		((JavaPackage) pkg).rename(alias);
	}

	public static boolean isParentRenamed(Object pkg) {
		return ((JavaPackage) pkg).isParentRenamed();
	}

	public static boolean isDescendantOf(Object pkg, Object ancestor) {
		return ((JavaPackage) pkg).isDescendantOf((JavaPackage) ancestor);
	}

	public static int getDefPos(Object pkg) {
		return ((JavaPackage) pkg).getDefPos();
	}

	public static List<?> getUseIn(Object pkg) {
		return ((JavaPackage) pkg).getUseIn();
	}

	public static void removeAlias(Object pkg) {
		((JavaPackage) pkg).removeAlias();
	}

	public static int compare(Object pkg1, Object pkg2) {
		return ((JavaPackage) pkg1).compareTo((JavaPackage) pkg2);
	}
}

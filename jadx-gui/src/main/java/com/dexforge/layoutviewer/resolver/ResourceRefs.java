package com.dexforge.layoutviewer.resolver;

final class ResourceRefs {
	private ResourceRefs() {
	}

	static String fromPath(String path) {
		int slash = path.indexOf('/');
		int nextSlash = path.indexOf('/', slash + 1);
		int dot = path.lastIndexOf('.');
		if (slash == -1 || nextSlash == -1 || dot <= nextSlash) {
			return null;
		}
		String type = path.substring(slash + 1, nextSlash);
		int qualifier = type.indexOf('-');
		if (qualifier != -1) {
			type = type.substring(0, qualifier);
		}
		String name = path.substring(nextSlash + 1, dot);
		return "@" + type + "/" + name;
	}

	static String drawableRef(String value) {
		if (value.startsWith("@")) {
			return value;
		}
		String ref = fromPath(value);
		return ref != null ? ref : value;
	}
}

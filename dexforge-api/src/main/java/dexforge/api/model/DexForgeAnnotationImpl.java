package dexforge.api.model;

import dexforge.api.model.metadata.DexForgeAnnotation;

final class DexForgeAnnotationImpl implements DexForgeAnnotation {
	private final AnnType type;
	private final String data;

	DexForgeAnnotationImpl(AnnType type, String data) {
		this.type = type;
		this.data = data;
	}

	@Override
	public AnnType getAnnType() {
		return type;
	}

	@Override
	public String getData() {
		return data;
	}
}

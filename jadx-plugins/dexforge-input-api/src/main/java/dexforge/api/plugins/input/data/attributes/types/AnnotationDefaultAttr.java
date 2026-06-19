package dexforge.api.plugins.input.data.attributes.types;

import dexforge.api.plugins.input.data.annotations.EncodedValue;
import dexforge.api.plugins.input.data.attributes.IJadxAttrType;
import dexforge.api.plugins.input.data.attributes.IJadxAttribute;
import dexforge.api.plugins.input.data.attributes.JadxAttrType;
import dexforge.api.plugins.input.data.attributes.PinnedAttribute;

public class AnnotationDefaultAttr extends PinnedAttribute {

	private final EncodedValue value;

	public AnnotationDefaultAttr(EncodedValue value) {
		this.value = value;
	}

	public EncodedValue getValue() {
		return value;
	}

	@Override
	public IJadxAttrType<? extends IJadxAttribute> getAttrType() {
		return JadxAttrType.ANNOTATION_DEFAULT;
	}

	@Override
	public String toString() {
		return "ANNOTATION_DEFAULT: " + value;
	}
}

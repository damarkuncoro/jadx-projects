package dexforge.plugins.input.java.data.attributes.types;

import dexforge.api.plugins.input.data.annotations.EncodedValue;
import dexforge.plugins.input.java.data.attributes.IJavaAttribute;
import dexforge.plugins.input.java.data.attributes.IJavaAttributeReader;

public class ConstValueAttr implements IJavaAttribute {

	private final EncodedValue value;

	public ConstValueAttr(EncodedValue value) {
		this.value = value;
	}

	public EncodedValue getValue() {
		return value;
	}

	public static IJavaAttributeReader reader() {
		return (clsData, reader) -> new ConstValueAttr(clsData.getConstPoolReader().readAsEncodedValue(reader.readU2()));
	}
}

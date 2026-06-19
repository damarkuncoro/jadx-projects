package dexforge.plugins.input.java.data.attributes.types;

import dexforge.api.plugins.input.data.annotations.EncodedValue;
import dexforge.api.plugins.input.data.attributes.types.AnnotationDefaultAttr;
import dexforge.plugins.input.java.data.attributes.EncodedValueReader;
import dexforge.plugins.input.java.data.attributes.IJavaAttribute;
import dexforge.plugins.input.java.data.attributes.IJavaAttributeReader;
import dexforge.plugins.input.java.data.attributes.JavaAttrStorage;
import dexforge.plugins.input.java.data.attributes.JavaAttrType;

public class JavaAnnotationDefaultAttr extends AnnotationDefaultAttr implements IJavaAttribute {

	public JavaAnnotationDefaultAttr(EncodedValue value) {
		super(value);
	}

	public static IJavaAttributeReader reader() {
		return (clsData, reader) -> new JavaAnnotationDefaultAttr(EncodedValueReader.read(clsData, reader));
	}

	public static AnnotationDefaultAttr convert(JavaAttrStorage attributes) {
		return attributes.get(JavaAttrType.ANNOTATION_DEFAULT);
	}
}

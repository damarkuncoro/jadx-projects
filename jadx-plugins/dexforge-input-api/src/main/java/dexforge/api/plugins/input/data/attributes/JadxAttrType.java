package dexforge.api.plugins.input.data.attributes;

import dexforge.api.plugins.input.data.annotations.EncodedValue;
import dexforge.api.plugins.input.data.attributes.types.AnnotationDefaultAttr;
import dexforge.api.plugins.input.data.attributes.types.AnnotationDefaultClassAttr;
import dexforge.api.plugins.input.data.attributes.types.AnnotationMethodParamsAttr;
import dexforge.api.plugins.input.data.attributes.types.AnnotationsAttr;
import dexforge.api.plugins.input.data.attributes.types.ExceptionsAttr;
import dexforge.api.plugins.input.data.attributes.types.InnerClassesAttr;
import dexforge.api.plugins.input.data.attributes.types.MethodParametersAttr;
import dexforge.api.plugins.input.data.attributes.types.SignatureAttr;
import dexforge.api.plugins.input.data.attributes.types.SourceFileAttr;

public final class JadxAttrType<T extends IJadxAttribute> implements IJadxAttrType<T> {

	// class, method, field
	public static final JadxAttrType<AnnotationsAttr> ANNOTATION_LIST = bind();
	public static final JadxAttrType<SignatureAttr> SIGNATURE = bind();

	// class
	public static final JadxAttrType<SourceFileAttr> SOURCE_FILE = bind();
	public static final JadxAttrType<InnerClassesAttr> INNER_CLASSES = bind();
	public static final JadxAttrType<AnnotationDefaultClassAttr> ANNOTATION_DEFAULT_CLASS = bind(); // dex specific

	// field
	public static final JadxAttrType<EncodedValue> CONSTANT_VALUE = bind();

	// method
	public static final JadxAttrType<AnnotationMethodParamsAttr> ANNOTATION_MTH_PARAMETERS = bind();
	public static final JadxAttrType<AnnotationDefaultAttr> ANNOTATION_DEFAULT = bind();
	public static final JadxAttrType<ExceptionsAttr> EXCEPTIONS = bind();
	public static final JadxAttrType<MethodParametersAttr> METHOD_PARAMETERS = bind();

	private static <T extends IJadxAttribute> JadxAttrType<T> bind() {
		return new JadxAttrType<>();
	}

	private JadxAttrType() {
	}
}

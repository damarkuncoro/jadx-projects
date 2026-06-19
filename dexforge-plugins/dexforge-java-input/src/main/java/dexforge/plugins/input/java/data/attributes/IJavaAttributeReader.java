package dexforge.plugins.input.java.data.attributes;

import dexforge.plugins.input.java.data.DataReader;
import dexforge.plugins.input.java.data.JavaClassData;

public interface IJavaAttributeReader {
	IJavaAttribute read(JavaClassData clsData, DataReader reader);
}

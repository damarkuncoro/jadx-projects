package dexforge.core.parser.dex.sections;

import java.util.HashMap;
import java.util.Map;

import dexforge.core.parser.dex.io.DexByteReader;
import dexforge.core.parser.dex.io.Leb128;
import dexforge.core.parser.dex.model.DexClass;
import dexforge.core.parser.dex.model.DexEncodedField;

/**
 * Resolves Android Resource IDs (0x7f...) to human-readable names
 * by scanning R classes within the DEX file.
 */
public final class DexResourceResolver {
    private final DexByteReader reader;
    private final Map<Integer, String> idToNameMap = new HashMap<>();
    private boolean initialized = false;

    public DexResourceResolver(DexByteReader reader) {
        this.reader = reader;
    }

    /**
     * Scans all classes in the indexer to find R$ classes and build the map.
     */
    public void layout(DexClassPool classPool, DexFieldPool fieldPool, DexClassDataParser dataParser) {
        if (initialized) return;

        for (int i = 0; i < classPool.getSize(); i++) {
            DexClass clazz = classPool.getClassDef(i);
            String name = clazz.getName();

            // Check if it's an R class (e.g., Lcom/example/R$string;)
            if (name.contains("/R$")) {
                int dollarIdx = name.lastIndexOf('$');
                String type = name.substring(dollarIdx + 1, name.length() - 1);
                parseRClass(clazz, type, fieldPool, dataParser);
            }
        }
        initialized = true;
    }

    private void parseRClass(DexClass clazz, String resType, DexFieldPool fieldPool, DexClassDataParser dataParser) {
        if (clazz.getClassDataOff() == 0 || clazz.getStaticValuesOff() == 0) return;

        var data = dataParser.parse(clazz.getClassDataOff());
        if (data == null || data.staticFields.isEmpty()) return;

        DexByteReader valReader = reader.at(clazz.getStaticValuesOff());
        int size = Leb128.readUleb128(valReader);

        // We only care about the values that correspond to the static fields
        for (int i = 0; i < Math.min(size, data.staticFields.size()); i++) {
            DexEncodedField field = data.staticFields.get(i);
            Object val = DexValueParser.parseValue(valReader);

            if (val instanceof Integer) {
                int resId = (Integer) val;
                if (isResourceId(resId)) {
                    String fieldName = fieldPool.getFieldName(field.getFieldIndex());
                    idToNameMap.put(resId, "R." + resType + "." + fieldName);
                }
            }
        }
    }

    public String resolve(int resId) {
        return idToNameMap.get(resId);
    }

    public boolean isResourceId(int val) {
        return (val >= 0x7f010000 && val <= 0x7fffffff) || (val >= 0x01010000 && val <= 0x01ffffff);
    }
}

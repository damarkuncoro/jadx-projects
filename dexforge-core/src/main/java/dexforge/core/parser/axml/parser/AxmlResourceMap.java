package dexforge.core.parser.axml.parser;

import dexforge.core.parser.arsc.io.ArscReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Parser for the RES_XML_RESOURCE_MAP chunk in AXML files.
 * Maps string indices to Android resource IDs.
 */
public final class AxmlResourceMap {
    private final Map<Integer, Integer> indexToResourceId = new HashMap<>();

    public AxmlResourceMap(ArscReader reader, int offset) {
        reader.setPosition(offset);
        int type = reader.readInt(); // 0x00080180
        int size = reader.readInt();

        int entryCount = (size - 8) / 4;
        for (int i = 0; i < entryCount; i++) {
            indexToResourceId.put(i, reader.readInt());
        }
    }

    public int getResourceId(int stringIndex) {
        return indexToResourceId.getOrDefault(stringIndex, 0);
    }

    public Map<Integer, Integer> getMappings() {
        return new HashMap<>(indexToResourceId);
    }
}

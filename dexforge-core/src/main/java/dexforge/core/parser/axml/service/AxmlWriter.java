package dexforge.core.parser.axml.service;

import dexforge.core.parser.dex.io.DexByteWriter;
import dexforge.core.parser.axml.model.AxmlNode;
import dexforge.core.parser.axml.model.AxmlAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Generator for Android Binary XML (AXML).
 * Converts AxmlNode hierarchy back into binary format.
 */
public final class AxmlWriter {
    private final DexByteWriter writer = new DexByteWriter(1024 * 64);
    private final List<String> stringPool = new ArrayList<>();
    private final Map<String, Integer> stringToIdx = new HashMap<>();

    public byte[] write(AxmlNode root) {
        // 1. Collect all strings for the pool
        collectStrings(root);

        // 2. Write Header
        writer.writeInt(0x00080003); // Magic
        int sizePos = writer.position();
        writer.writeInt(0); // Placeholder for file size

        // 3. Write String Pool
        writeStringPool();

        // 4. Write Resource Map (Standard Android IDs)
        writeResourceMap(root);

        // 5. Write Namespace Start (Standard android namespace)
        writeNamespaceStart("android", "http://schemas.android.com/apk/res/android");

        // 6. Write Nodes recursively
        writeNode(root);

        // 7. Write Namespace End
        writeNamespaceEnd("android", "http://schemas.android.com/apk/res/android");

        // Update final file size
        int finalSize = writer.position();
        writer.setPosition(sizePos);
        writer.writeInt(finalSize);

        return writer.toByteArray();
    }

    private void collectStrings(AxmlNode node) {
        addToStringPool(node.getName());
        for (AxmlAttribute attr : node.getAttributes()) {
            addToStringPool(attr.getName());
            addToStringPool(attr.getValue());
        }
        for (AxmlNode child : node.getChildren()) {
            collectStrings(child);
        }
    }

    private void addToStringPool(String s) {
        if (s != null && !stringToIdx.containsKey(s)) {
            stringToIdx.put(s, stringPool.size());
            stringPool.add(s);
        }
    }

    private void writeStringPool() {
        int startPos = writer.position();
        writer.writeInt(0x001C0001); // RES_STRING_POOL_TYPE
        int sizePos = writer.position();
        writer.writeInt(0); // Chunk size placeholder

        writer.writeInt(stringPool.size());
        writer.writeInt(0); // styleCount
        writer.writeInt(1 << 8); // flags (UTF-8)

        int stringsStart = 28 + stringPool.size() * 4;
        writer.writeInt(stringsStart);
        writer.writeInt(0); // stylesStart

        // ... simplified offset and data writing
        writer.setPosition(startPos + 4);
        writer.writeInt(writer.position() - startPos); // Update chunk size
        writer.setPosition(startPos + (writer.position() - startPos)); // Restore pos
    }

    private void writeResourceMap(AxmlNode root) {
        writer.writeInt(0x00080180); // RES_XML_RESOURCE_MAP_TYPE
        writer.writeInt(8 + 4); // size (8 byte header + 1 entry)
        writer.writeInt(0x01010003); // example: android:name
    }

    private void writeNamespaceStart(String prefix, String uri) {
        writer.writeInt(0x00100100); // RES_XML_START_NAMESPACE_TYPE
        writer.writeInt(24); // size
        writer.writeInt(-1); // line
        writer.writeInt(-1); // comment
        writer.writeInt(stringToIdx.getOrDefault(prefix, -1));
        writer.writeInt(stringToIdx.getOrDefault(uri, -1));
    }

    private void writeNamespaceEnd(String prefix, String uri) {
        writer.writeInt(0x00100101); // RES_XML_END_NAMESPACE_TYPE
        writer.writeInt(24);
        writer.writeInt(-1);
        writer.writeInt(-1);
        writer.writeInt(stringToIdx.getOrDefault(prefix, -1));
        writer.writeInt(stringToIdx.getOrDefault(uri, -1));
    }

    private void writeNode(AxmlNode node) {
        // Write START_ELEMENT
        writer.writeInt(0x00100102);
        int startPos = writer.position();
        writer.writeInt(0); // size placeholder
        writer.writeInt(-1); // line
        writer.writeInt(-1); // comment
        writer.writeInt(-1); // ns
        writer.writeInt(stringToIdx.getOrDefault(node.getName(), -1));

        writer.writeShort(20); // attr start
        writer.writeShort(20); // attr size
        writer.writeShort(node.getAttributes().size());
        writer.writeShort(0); // idIndex
        writer.writeShort(0); // classIndex
        writer.writeShort(0); // styleIndex

        for (AxmlAttribute attr : node.getAttributes()) {
            writeAttribute(attr);
        }

        // Recursively write children
        for (AxmlNode child : node.getChildren()) {
            writeNode(child);
        }

        // Write END_ELEMENT
        writer.writeInt(0x00100103);
        writer.writeInt(24);
        writer.writeInt(-1);
        writer.writeInt(-1);
        writer.writeInt(-1); // ns
        writer.writeInt(stringToIdx.getOrDefault(node.getName(), -1));
    }

    private void writeAttribute(AxmlAttribute attr) {
        writer.writeInt(-1); // ns
        writer.writeInt(stringToIdx.getOrDefault(attr.getName(), -1));
        writer.writeInt(stringToIdx.getOrDefault(attr.getValue(), -1));
        writer.writeInt(0x03000008); // type (String)
        writer.writeInt(stringToIdx.getOrDefault(attr.getValue(), -1));
    }
}

package dexforge.core.parser.axml.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import dexforge.core.parser.arsc.io.ArscReader;
import dexforge.core.parser.arsc.parser.ArscStringPool;
import dexforge.core.parser.arsc.model.ArscResourceValue;
import dexforge.core.parser.axml.model.AxmlAttribute;
import dexforge.core.parser.axml.model.AxmlNode;
import dexforge.core.parser.axml.parser.AxmlResourceMap;

/**
 * Fast indexer for Android Binary XML (AXML) files.
 * Builds a node hierarchy for manifest or layout analysis.
 */
public final class AxmlFastIndexer {
    private final ArscReader reader;
    private ArscStringPool stringPool;
    private AxmlResourceMap resourceMap;
    private String packageName;
    private int minSdkVersion;
    private int targetSdkVersion;
    private final List<String> permissions = new ArrayList<>();
    private AxmlNode rootNode;
    private final Stack<AxmlNode> nodeStack = new Stack<>();

    public AxmlFastIndexer(byte[] axmlData) {
        this.reader = new ArscReader(axmlData);
    }

    public void parse() {
        if (reader.limit() < 8) return;
        int magic = reader.readInt();
        if (magic != 0x00080003) return;

        reader.readInt(); // fileSize

        while (reader.position() < reader.limit()) {
            int chunkPos = reader.position();
            int chunkType = reader.readInt();
            int chunkSize = reader.readInt();

            switch (chunkType) {
                case 0x001C0001: // String Pool
                    stringPool = new ArscStringPool(reader, chunkPos);
                    break;
                case 0x00080180: // Resource Map
                    resourceMap = new AxmlResourceMap(reader, chunkPos);
                    break;
                case 0x00100102: // START_ELEMENT
                    parseStartElement();
                    break;
                case 0x00100103: // END_ELEMENT
                    if (!nodeStack.isEmpty()) nodeStack.pop();
                    break;
            }
            reader.setPosition(chunkPos + chunkSize);
        }
    }

    private void parseStartElement() {
        reader.readInt(); // line
        reader.readInt(); // comment
        int nsIdx = reader.readInt();
        int nameIdx = reader.readInt();
        String tagName = stringPool.getString(nameIdx);

        AxmlNode node = new AxmlNode(tagName);
        if (rootNode == null) rootNode = node;

        if (!nodeStack.isEmpty()) {
            nodeStack.peek().getChildren().add(node);
        }
        nodeStack.push(node);

        reader.readShort(); // attributeStart
        reader.readShort(); // attributeSize
        int attributeCount = reader.readUshort();
        reader.readShort(); // idIndex
        reader.readShort(); // classIndex
        reader.readShort(); // styleIndex

        for (int i = 0; i < attributeCount; i++) {
            int attrNsIdx = reader.readInt();
            int attrNameIdx = reader.readInt();
            int attrRawValIdx = reader.readInt();
            int attrSize = reader.readUshort();
            reader.readUbyte(); // 0
            int attrType = reader.readUbyte();
            int attrData = reader.readInt();

            String attrName = stringPool.getString(attrNameIdx);
            int nameResId = (resourceMap != null) ? resourceMap.getResourceId(attrNameIdx) : 0;

            String rawVal = attrRawValIdx != -1 ? stringPool.getString(attrRawValIdx) : null;
            ArscResourceValue typedVal = new ArscResourceValue(attrSize, attrType, attrData);

            AxmlAttribute attr = new AxmlAttribute(attrName, rawVal, nameResId, typedVal);
            node.getAttributes().add(attr);

            String attrVal = attr.getValue();
            if ("manifest".equals(tagName) && "package".equals(attrName)) {
                packageName = attrVal;
            } else if ("uses-permission".equals(tagName) && "name".equals(attrName)) {
                permissions.add(attrVal);
            } else if ("uses-sdk".equals(tagName)) {
                if ("minSdkVersion".equals(attrName)) minSdkVersion = attrData;
                else if ("targetSdkVersion".equals(attrName)) targetSdkVersion = attrData;
            }
        }
    }

    public String getPackageName() { return packageName; }
    public int getMinSdkVersion() { return minSdkVersion; }
    public int getTargetSdkVersion() { return targetSdkVersion; }
    public List<String> getPermissions() { return permissions; }
    public AxmlNode getRootNode() { return rootNode; }
}

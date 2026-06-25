package dexforge.core.parser.dex.sections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dexforge.core.parser.dex.io.DexByteReader;
import dexforge.core.parser.dex.io.Leb128;
import dexforge.core.parser.dex.model.DexAnnotation;

/**
 * Parser for DEX annotations.
 */
public final class DexAnnotationParser {
    private final DexByteReader reader;
    private final DexTypePool typePool;
    private final DexStringPool stringPool;

    public DexAnnotationParser(DexByteReader reader, DexTypePool typePool, DexStringPool stringPool) {
        this.reader = reader;
        this.typePool = typePool;
        this.stringPool = stringPool;
    }

    /**
     * Parses a set of annotations from an annotation_set_item offset.
     */
    public List<DexAnnotation> parseAnnotationSet(int offset) {
        if (offset == 0) return Collections.emptyList();

        DexByteReader setReader = reader.at(offset);
        int size = setReader.readInt();
        List<DexAnnotation> annotations = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            int annotationOff = setReader.readInt();
            annotations.add(parseAnnotationItem(annotationOff));
        }
        return annotations;
    }

    private DexAnnotation parseAnnotationItem(int offset) {
        DexByteReader itemReader = reader.at(offset);
        int visibility = itemReader.readUbyte();

        // encoded_annotation
        int typeIdx = Leb128.readUleb128(itemReader);
        int size = Leb128.readUleb128(itemReader);

        String type = typePool.getTypeName(typeIdx);
        Map<String, Object> elements = new HashMap<>();

        for (int i = 0; i < size; i++) {
            int nameIdx = Leb128.readUleb128(itemReader);
            Object value = DexValueParser.parseValue(itemReader);
            elements.put(stringPool.getString(nameIdx), value);
        }

        return new DexAnnotation(type, visibility, elements);
    }

    /**
     * Extracts annotations for a class from its annotations_directory_item.
     */
    public List<DexAnnotation> getClassAnnotations(int directoryOff) {
        if (directoryOff == 0) return Collections.emptyList();

        int classAnnotationsOff = reader.at(directoryOff).readInt();
        return parseAnnotationSet(classAnnotationsOff);
    }

    /**
     * Extracts annotations for a field from the directory.
     */
    public List<DexAnnotation> getFieldAnnotations(int directoryOff, int fieldIdx) {
        if (directoryOff == 0) return Collections.emptyList();

        DexByteReader dirReader = reader.at(directoryOff + 4);
        int fieldsSize = dirReader.readInt();
        int methodsSize = dirReader.readInt();
        int paramsSize = dirReader.readInt();

        for (int i = 0; i < fieldsSize; i++) {
            int idx = dirReader.readInt();
            int off = dirReader.readInt();
            if (idx == fieldIdx) return parseAnnotationSet(off);
        }
        return Collections.emptyList();
    }

    /**
     * Extracts annotations for a method from the directory.
     */
    public List<DexAnnotation> getMethodAnnotations(int directoryOff, int methodIdx) {
        if (directoryOff == 0) return Collections.emptyList();

        DexByteReader dirReader = reader.at(directoryOff + 4);
        int fieldsSize = dirReader.readInt();
        int methodsSize = dirReader.readInt();
        int paramsSize = dirReader.readInt();

        // Skip fields
        dirReader.setPosition(dirReader.position() + (fieldsSize * 8));

        for (int i = 0; i < methodsSize; i++) {
            int idx = dirReader.readInt();
            int off = dirReader.readInt();
            if (idx == methodIdx) return parseAnnotationSet(off);
        }
        return Collections.emptyList();
    }
}

package dexforge.core.parser.assets;

import dexforge.core.parser.dex.io.DexByteReader;

/**
 * Basic parser for SQLite database files found in assets.
 * Validates header and extracts basic metadata.
 */
public final class SqliteParser {
    private final DexByteReader reader;

    public SqliteParser(byte[] data) {
        this.reader = new DexByteReader(data);
    }

    public boolean isValid() {
        if (reader.limit() < 16) return false;
        reader.setPosition(0);
        // SQLite Magic: "SQLite format 3\0"
        byte[] magic = reader.readByteArray(16);
        String magicStr = new String(magic);
        return magicStr.startsWith("SQLite format 3");
    }

    public int getPageSize() {
        if (!isValid()) return 0;
        reader.setPosition(16);
        return reader.readUshort();
    }

    public int getTableCount() {
        // Table count is not directly in the header,
        // would require parsing the schema table in page 1.
        return 0;
    }
}

# DEX Parser Development Notes

## Current Architecture: Fast Indexer
Unlike `jadx-core` which is a full decompiler, `dexforge-core` focuses on high-speed metadata indexing and lightweight static analysis.

### Key Differences vs JADX-Core:
1. **No CFG**: We use linear scanning for speed instead of building a Control Flow Graph.
2. **No SSA**: We don't perform register renaming or data flow analysis.
3. **Lazy Loading**: Data is only parsed when requested (e.g., class data, annotations).

## Roadmap & Improvements:
- [x] Basic Metadata Indexing (Strings, Types, Protos, Fields, Methods).
- [x] Class Hierarchy & Access Flags.
- [x] Internal Class Data (Encoded Fields/Methods).
- [x] **Global XREFs**: Bi-directional tracking for Strings, Methods, Fields, and Types.
- [x] **Annotation Parsing**: Parsing of class-level annotations and their elements.
- [x] **Robust Instruction Decoding**: 100% sync during linear scan with full opcode mapping.
- [x] **Exception Handling Mapping**: Identifying try-catch blocks and handlers.
- [x] **Debug Info Parsing**: Extracting line number tables and local variable metadata.
- [x] **Resource ID Resolver**: Linking DEX integers to Android Resources by scanning R classes.
- [x] **Smali Disassembler/Assembler**: Bidirectional conversion between Bytecode and Smali.
- [x] **APK Toolchain**: Loading, Rebuilding, and Signing (V1) APK files.
- [ ] **Advanced Data Flow** (Future): Simple register tracking within methods.
- [ ] **Method-level Graphing** (Future): Generating simple flow charts for methods.

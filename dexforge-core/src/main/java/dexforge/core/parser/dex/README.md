# DexForge Core Parser

A high-performance DEX parser optimized for fast indexing and lightweight static analysis.

## Key Features

*   **Fast Indexing**: Instant access to DEX pools (Strings, Types, Protos, Fields, Methods).
*   **Class Hierarchy**: Deep analysis of class definitions, interfaces, and superclasses.
*   **Global XREFs**: Bi-directional cross-references for strings, methods, and fields across the entire DEX.
*   **Annotation Support**: Parsing of class-level annotations.
*   **Debug Info**: Extraction of line number mappings for accurate crash report analysis.
*   **Instruction Scanning**: Linear decoding of Dalvik bytecode to identify string usages and method calls.
*   **Smali Toolchain**: Full Disassembler and initial Assembler for modifying bytecode.
*   **APK Management**: Integrated APK Loader, Rebuilder, and V1 Signer.
*   **Resource Resolution**: Automatically maps hex resource IDs (e.g., `0x7f...`) to human-readable names (e.g., `R.string.app_name`).

## Usage Examples

### Loading an APK
```java
ApkLoader loader = new ApkLoader();
DexProject project = loader.load(new File("app.apk"));
```

### Modifying and Rebuilding
```java
// 1. Load APK
ApkLoader loader = new ApkLoader();
DexProject project = loader.load(new File("input.apk"));

// 2. Perform edits (e.g., via SmaliAssembler)
// ...

// 3. Rebuild and Sign
ApkRebuilder rebuilder = new ApkRebuilder();
rebuilder.rebuild(new File("input.apk"), new File("output-unsigned.apk"), project.getIndexers().get(0));

ApkSigner signer = new ApkSigner();
signer.sign(new File("output-unsigned.apk"), new File("output-signed.apk"));
```

### Searching for String Usages
```java
// Find every method that uses a specific sensitive string
Map<String, List<String>> usages = indexer.searchGlobalStringUsages("https://api.secret.com");
usages.forEach((str, methods) -> {
    System.out.println("Sensitive string found in: " + methods);
});
```

### Finding Method Callers (XREFs)
```java
// Find all callers of an Android API
Map<String, List<String>> callers = indexer.searchGlobalMethodCalls("getDeviceId");
```

### Resolving Resource IDs
```java
// Resolve an ID found in bytecode
int resId = 0x7f0b0021;
String resName = indexer.resolveResource(resId);
System.out.println("Resource ID " + Integer.toHexString(resId) + " is " + resName);
```

### Analyzing Class Internal Data
```java
DexClass clazz = indexer.getClasses().get(0);
// Populate fields and methods (lazy loading)
indexer.fillClassData(clazz);
List<String> methods = indexer.getMethodsInClass(clazz);
```

## Performance
Designed to be significantly faster than full decompilers by avoiding expensive CFG construction and SSA transformations, making it ideal for GUI tools and rapid scanners.

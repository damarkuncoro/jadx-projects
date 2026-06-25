# Rencana Pengembangan Parser DexForge

Dokumen ini melacak status pengembangan parser di DexForge.

## 1. Parser yang Sudah Diimplementasikan (Update)

### A. Core Parsers
- **DEX Parser**: Full indexing, cross-references (XREF: String, Method, Field, Type, Resource), string/type/field/method pools. (ENHANCED)
- **ARSC Parser**: Membaca paket, tipe, dan entri resource. Terintegrasi dengan `ResourceResolver`.
- **AXML Parser**: Full node hierarchy building untuk manifest dan layout. (ENHANCED: Support Resource Map & Typed Values)
- **AXML Decompiler**: Dekompilasi binary XML kembali ke teks XML yang terbaca. (ENHANCED: 100% Accurate Name/Value Resolution)
- **Java Decompiler Engine**: Dekompilasi berbasis CFG, Dominator Tree, Type Inference, SSA-lite Naming, Expression Reconstruction, dan If/Else/Loop/Switch/Try-Catch support. (ENHANCED: Inline Security, JNI Bridge & Annotation Support)
- **CodeInsightAnnotator**: Komponen 'Bridge' yang menyisipkan wawasan keamanan, JNI, dan metadata langsung ke dalam kode dekompilasi. (ENHANCED)
- **Layout Analyzer**: Ekstraksi struktur UI, statistik View, dan dependensi resource dari file layout XML. (ENHANCED)
- **Visual Layout Renderer**: Transformasi hirarki node UI ke format JSON untuk visualisasi desain.
- **Visual UI Editor**: Engine untuk pengeditan komponen UI secara langsung (Update property, Add/Delete node) dengan sinkronisasi binary AXML. (NEW)

### B. Smali Tooling
- **Smali Lexer/Parser**: Pipeline lengkap dari teks ke AST.
- **Smali Semantic Analyzer**: Validasi register dan opcode.
- **Smali Writer**: Generator teks smali dari model DEX.
- **Smali Assembler**: Pengumpulan metadata (DEX Pool Manager) dan instruksi Dalvik. (ENHANCED: Extended Opcode Support)

### C. Advanced & Security Parsers
- **CFG Parser**: Membangun Control Flow Graph dengan dukungan penanganan eksepsi (Try-Catch). (ENHANCED)
- **Call Graph Analyzer**: Membangun peta hubungan pemanggilan metode secara global dengan Heatmap. (ENHANCED)
- **Data Flow Analyzer**: Analisis global menggunakan algoritma Worklist dengan penggabungan tipe data dan propagasi taint. (ENHANCED: Generic DFA Engine & Inter-procedural Support)
- **Taint Analyzer**: Melacak aliran data sensitif dari sumber (Sources) ke tujuan berbahaya (Sinks) di seluruh alur kendali. (ENHANCED: Powered by Advanced Engine)
- **Vulnerability Scanner**: Agregasi temuan keamanan (Hardcoded keys, JWT, insecure patterns, component, permission, & service audit). (ENHANCED: Added Service & Intent Filter Audit)
- **Malware Detector**: Deteksi packer (Qihoo, Tencent, Baidu) melalui library native.
- **JNI Bridge Mapper**: Menghubungkan metode 'native' Java dengan implementasi C/C++ di file .so.
- **Project Intelligence**: Agregasi otomatis temuan (Framework, Packer, Security Scoring, Hot Methods, API Mapping). (ENHANCED)
- **GraphExporter**: Ekspor CFG dan Call Graph ke format DOT dengan visualisasi beban panas. (ENHANCED)
- **StringPatternAnalyzer**: Ekstraksi otomatis URL, IP, API Key (AWS, Google), dan Firebase. (ENHANCED)
- **Signature Parser**: Ekstraksi sertifikat X.509 dari APK (v1 scheme).
- **Kotlin Metadata Parser**: Dekode anotasi `@kotlin.Metadata` dengan heuristik rekonstruksi fungsi.
- **ELF Parser**: Identifikasi simbol JNI dalam library native `.so` (Supports 32/64 bit). (ENHANCED)
- **Json/SQLite/Protobuf Parsers**: Analisis aset aplikasi untuk konfigurasi dan data binary. (ENHANCED)
- **Manifest Analyzer**: Ekstraksi komponen Android dan audit flag keamanan tingkat lanjut. (ENHANCED)

---

## 2. Parser dalam Pengembangan / Rencana Mendatang

### A. Deep Analysis
- **Kotlin Metadata Enhancer**: Rekonstruksi penuh struktur kelas Kotlin menggunakan parser protobuf.

### B. Rebuilding
- **Full Smali Assembler**: Mesin penuh untuk mengubah AST Smali menjadi bytecode DEX.
- **AXML Writer**: Generator binary XML dari hirarki node (Manifest/Layout editor). (ENHANCED)
- **APK Rebuilder**: Fondasi pengemasan kembali file-file menjadi struktur APK (ZIP).
- **ApkSignerV2**: Implementasi penandatanganan APK Skema v2. (ENHANCED)

---

## 3. Peta Jalan (Roadmap)

 Fase | Fokus | Status |
 :--- | :--- | :--- |
 **Fase 1** | Core Bytecode | Selesai |
 **Fase 2** | Resource & Layout | Selesai |
 **Fase 3** | Deep Analysis | Selesai (CFG, Call Graph, Data Flow, Decompiler) |
 **Fase 4** | Assets & Security | Selesai (Signature, JSON, SQLite, Malware, Vulnerability) |
 **Fase 5** | Rebuilding | Berjalan (Assembler & AXML Writer Foundation) |

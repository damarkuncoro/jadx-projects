# Rencana DEX Parser — High Performance Low-Level API

Dokumen ini menjelaskan rencana pembangunan **DEX Parser** asli DexForge. Parser ini dirancang sebagai komponen internal di dalam `dexforge-core` untuk memberikan akses cepat ke metadata `.dex` tanpa harus menunggu engine decompiler penuh (seperti JADX) melakukan inisialisasi.

---

### Peran dalam Arsitektur DexForge

Parser ini akan memperkuat `dexforge-core` dalam tiga aspek utama:

1.  **Fast Metadata Extraction**: Mengambil daftar class, method, dan field dalam milidetik (cocok untuk indexing pencarian cepat).
2.  **Integrity & Security Check**: Melakukan verifikasi checksum (Adler-32) dan signature (SHA-1) secara mandiri untuk deteksi tampering APK.
3.  **Engine Independence**: Memberikan fondasi bagi DexForge untuk melakukan analisis bytecode dasar bahkan jika engine dekompilasi utama sedang bermasalah.

---

### Struktur Modul (Refactored)

Lokasi: `dexforge-core/src/main/java/dexforge/core/parser/dex/`

```
dex/
├── io/
│   ├── DexByteReader.java      # Little-endian access dengan buffer management
│   ├── Leb128.java            # High-performance variable-length decoding
│   └── Mutf8.java             # Modified UTF-8 decoder (Android spec)
├── sections/
│   ├── DexHeaderParser.java    # Parsing 112 byte header utama
│   ├── DexStringPool.java      # Indexing string_ids yang efisien
│   ├── DexTypePool.java        # Indexing type_ids
│   ├── DexClassParser.java     # Parsing class_defs dan class_data
│   └── DexAnnotationParser.java# Deep access ke metadata annotasi
├── model/
│   ├── DexHeader.java          # Value Object untuk info file DEX
│   ├── DexClassDef.java        # Struktur class low-level
│   └── DexMethodBody.java      # Offset bytecode dan register count
└── service/
    └── DexFastIndexer.java     # Integrasi ke Search API di dexforge-core
```

---

### Fase Implementasi (Support untuk Core)

**Fase 1 — Core IO & Integrity (Minggu 1)**
Fokus pada validasi binary APK/DEX.
- Implementasi `DexByteReader` yang mendukung `MappedByteBuffer` (untuk menangani file DEX berukuran besar dengan memory-efficient).
- Verifikasi otomatis saat file dibuka melalui `DexHeaderParser`.

**Fase 2 — Fast Indexing (Minggu 2)**
Fokus pada peningkatan kecepatan **Fluent Query API**.
- `DexFastIndexer` akan membaca `string_ids` dan `type_ids` secara paralel.
- Core akan menggunakan data ini untuk mengisi `search().classes()` jauh sebelum decompiler JADX selesai memproses bytecode.

**Fase 3 — Deep Metadata (Minggu 3)**
- Parsing `annotations_directory_item`.
- Mendukung fitur "Security Scanner" di Core untuk mendeteksi ijin (permissions) atau API sensitif secara langsung dari tabel string DEX.

---

### Peningkatan Kemampuan "dexforge-core"

Dengan parser ini, `dexforge-core` akan memiliki keunggulan berikut:

| Fitur | Standar Engine (JADX) | Dengan DexForge Native Parser |
| :--- | :--- | :--- |
| **Startup Time** | Menunggu dekompilasi penuh. | **Instant Indexing** (bisa mencari class saat file sedang di-load). |
| **Memory Usage** | Tinggi (menyimpan semua node). | **Low Memory** (hanya menyimpan offset/index penting). |
| **Tampering Detection**| Tergantung engine. | **Native Checksum Verification** (Adler32/SHA-1). |
| **Search Accuracy** | Hanya yang berhasil didecompile.| **Full DEX Coverage** (termasuk method yang mungkin gagal didecompile). |

---

### Referensi Teknis

- [Dalvik Executable (DEX) format](https://source.android.com/docs/core/runtime/dex-format)
- [AOSP - dex_file.h](https://android.googlesource.com/platform/art/+/master/libdexfile/dex/dex_file.h)

---

**Langkah Selanjutnya**:
Mulai implementasi `DexByteReader.java` di modul `dexforge-core` untuk mendukung pembacaan buffer yang cepat dan aman.

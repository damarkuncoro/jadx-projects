**ARSC parser** adalah bagian program yang membaca file **`resources.arsc`** di dalam APK Android.

Di APK, file ini berisi **database resource Android yang sudah dikompilasi**, misalnya:

```text
string, color, dimen, style, layout reference, drawable reference, id, attr, theme
```

Jadi kalau **smali parser** membaca kode bytecode, maka **ARSC parser** membaca resource table aplikasi.

Alurnya:

```text
APK
 ↓
resources.arsc
 ↓
ArscReader (Binary Reader)
 ↓
ArscFastIndexer (Chunk Parser)
 ↓
Resource Table Model (Package, Type, Entry)
 ↓
Resource Resolver
```

## Arsitektur Baru ARSC Parser

Untuk meningkatkan performa dan keterbacaan kode, arsitektur ARSC parser di **DexForge** dibagi menjadi beberapa bagian:

### 1. Model Data (`arsc/model/`)
Kami menggunakan model yang terstruktur untuk merepresentasikan hierarki resource:
- `ArscPackage`: Mewakili satu paket aplikasi (biasanya ID 0x7f).
- `ArscType`: Mewakili tipe resource (string, color, layout, dll).
- `ArscEntry`: Mewakili satu entri resource dengan nama dan nilai.
- `ArscResourceValue`: Menyimpan data mentah dan tipe nilai resource (misalnya reference, string, color).
- `ArscChunkHeader`: Informasi dasar tentang blok binary data.

### 2. IO & Parser (`arsc/io/` & `arsc/parser/`)
- `ArscReader`: Pembaca binary yang dioptimalkan untuk format ARSC (little-endian).
- `ArscStringPool`: Komponen khusus untuk mendekode tabel string yang digunakan secara global maupun dalam paket.

### 3. Service (`arsc/service/`)
- `ArscFastIndexer`: Mesin utama yang memindai file `.arsc` secara cepat dan membangun index pemetaan resource ID ke nama (misalnya `0x7f0f0001` -> `R.string.app_name`).

## Kenapa `resources.arsc` penting?

Karena di kode bytecode (DEX/Smali), resource diakses menggunakan ID numerik:

```smali
const v0, 0x7f0f0001
```

Dengan ARSC parser, DexForge dapat menerjemahkan angka tersebut menjadi nama yang bermakna di editor:

```java
setTitle(R.string.app_name); // Hasil pemetaan dari 0x7f0f0001
```

## Cara Kerja Resource ID

Resource ID Android memiliki format **`0xPPTTEEEE`**:
- **PP (Package ID)**: Biasanya `7f` untuk aplikasi pihak ketiga.
- **TT (Type ID)**: Indeks tipe (string, color, dll).
- **EEEE (Entry Index)**: Indeks urutan resource dalam tipe tersebut.

Contoh: `0x7f060001`
- `7f` = App Package
- `06` = Tipe (misal: color)
- `0001` = Entri pertama

## Implementasi di DexForge

Struktur folder yang digunakan:

```text
dexforge-core/src/main/java/dexforge/core/parser/arsc/
├── io/
│   └── ArscReader.java
├── model/
│   ├── ArscPackage.java
│   ├── ArscType.java
│   ├── ArscEntry.java
│   ├── ArscResourceValue.java
│   └── ArscChunkHeader.java
├── parser/
│   └── ArscStringPool.java
└── service/
    └── ArscFastIndexer.java
```

## Penggunaan di Plugin Lain
ARSC Parser juga digunakan oleh `AxmlFastIndexer` untuk memproses tabel string di dalam `AndroidManifest.xml` binary, sehingga meminimalkan duplikasi kode untuk pembacaan format String Pool Android.

## Fitur Mendatang
- **ArscResourceResolver**: Untuk mendekode nilai kompleks seperti `dimension` (dp, sp), `fraction`, dan resolusi `reference` antar paket.
- **Configuration Support**: Menangani resource yang memiliki variasi bahasa (`values-in`), orientasi, dan densitas layar.

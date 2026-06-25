**AXML parser** adalah parser untuk membaca **Android XML binary** di dalam APK (misalnya `AndroidManifest.xml` dan file layout).

## Arsitektur AXML Parser di DexForge

AXML Parser di **DexForge** dirancang agar modular dan efisien, berbagi komponen pembacaan String Pool dengan ARSC parser.

### 1. Struktur Folder (`axml/`)
Organisasi kode mengikuti standar modul lainnya di DexForge:

```text
dexforge-core/src/main/java/dexforge/core/parser/axml/
├── model/
│   ├── AxmlNode.java       (Mewakili Tag XML)
│   └── AxmlAttribute.java  (Mewakili Atribut Tag)
├── parser/
│   └── (Parser tingkat rendah untuk chunk XML)
└── service/
    ├── AxmlFastIndexer.java (Ekstraksi cepat metadata manifest)
    └── AxmlWriter.java      (Generator binary XML)
```

### 2. Komponen Utama

- **`AxmlFastIndexer`**: Dirancang untuk memproses `AndroidManifest.xml` dalam milidetik guna mendapatkan informasi penting seperti `package name`, `permissions`, `activities`, dll., sebelum seluruh APK selesai diproses.
- **`AxmlWriter`**: Menyediakan fondasi untuk mengkompilasi kembali XML teks menjadi format binary Android yang valid (berguna untuk fitur editing APK).
- **`ArscStringPool`**: Digunakan kembali untuk mendekode tabel string yang ada di dalam file binary XML.

## Kenapa AXML parser penting?

Android menyimpan file XML dalam format binary untuk efisiensi ruang dan kecepatan parsing di perangkat. Tanpa AXML parser, file-file tersebut tidak dapat dibaca oleh manusia.

Di dalam **DexForge**, AXML parser digunakan untuk:
1.  **Decode Manifest**: Membaca komponen aplikasi (Activity, Service, Receiver).
2.  **Analisis Izin**: Mengekstrak daftar permission yang diminta aplikasi.
3.  **Layout Viewer**: Mendekode file layout di `res/layout/*.xml` untuk ditampilkan di UI.
4.  **Resource Linkage**: Menghubungkan ID atribut ke nama aslinya menggunakan `ARSC Parser`.

## Struktur Binary XML (AXML)

Secara teknis, sebuah file AXML terdiri dari blok-blok (chunks):

```text
Header (Magic: 0x00080003)
String Pool (Daftar semua teks)
Resource Map (Mapping ID atribut Android)
Start Namespace
  Start Element (<tag>)
    Attributes (name="value")
  End Element (</tag>)
End Namespace
```

### Contoh Pemetaan Atribut
Dalam binary, atribut `android:name=".MainActivity"` disimpan sebagai indeks ke String Pool dan mungkin memiliki resource ID `0x01010003`. AXML parser bertugas menyatukan informasi ini kembali.

## Algoritma Fast Indexing

`AxmlFastIndexer` menggunakan pendekatan *stream-based* sederhana:
1.  Validasi Magic Number.
2.  Parse String Pool.
3.  Iterasi chunk hingga menemukan `START_TAG`.
4.  Jika tag adalah `<manifest>`, ambil atribut `package`.
5.  Jika tag adalah `<uses-permission>`, ambil atribut `android:name`.
6.  Berhenti jika informasi minimal sudah didapat (efisien).

## Integrasi dengan ARSC
Banyak nilai atribut di AXML berupa **Resource Reference** (misalnya `@string/app_name`). AXML parser membaca ID numeriknya (misal `0x7f0f0001`), dan `ArscFastIndexer` akan menerjemahkannya menjadi nama aslinya.

## Rencana Pengembangan
- **Visual Layout Renderer**: Menggunakan data dari `AxmlNode` untuk merender tampilan UI sederhana di dalam DexForge.
- **Manifest Editor**: Memungkinkan user menambah atau menghapus izin/komponen langsung dari UI dan menyimpannya kembali menggunakan `AxmlWriter`.
- **AxmlDecompiler**: Mesin dekompilasi penuh yang menghasilkan XML teks cantik dengan indentasi dan komentar dari binary XML.

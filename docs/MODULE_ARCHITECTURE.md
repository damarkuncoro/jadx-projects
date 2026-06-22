# Arsitektur Modul: dexforge-api vs dexforge-core

Dokumen ini menjelaskan tanggung jawab, batasan, dan hubungan antara dua modul utama dalam ekosistem DexForge.

---

## Ringkasan Perbandingan

| Fitur | **dexforge-api** | **dexforge-core** |
| :--- | :--- | :--- |
| **Tujuan** | Boundary Publik & Kontrak Domain | Implementasi Engine & Adapter Backend |
| **Ketergantungan JADX** | **Zero JADX Dependency** (Tidak boleh ada import `jadx.*`) | Mengelola dependensi ke `jadx-core` |
| **Konsumen Utama** | `dexforge-gui`, `dexforge-cli`, Plugin eksternal | `dexforge-api` (sebagai runtime provider) |
| **Isi Utama** | Model Domain (Class, Method, Project), Interface | JADX Bridge, Caching, Logic Decompiler |
| **Prinsip Arsitektur** | DDD (Domain Driven Design), SOLID | Infrastructure Layer, Adapter Pattern |
| **Stabilitas** | Sangat Stabil (Jarang berubah) | Fleksibel (Bisa berubah mengikuti update JADX) |

---

## 1. dexforge-api (The Interface)

Modul ini adalah "Wajah" dari DexForge. Ia mendefinisikan *apa* yang bisa dilakukan oleh sistem tanpa peduli *bagaimana* cara melakukannya.

### Karakteristik:
- **Clean Architecture**: Bertindak sebagai lapisan *Domain* dan *API Boundary*.
- **Opaque Implementation**: Menggunakan `Object delegate` untuk menyembunyikan detail mesin dekompilasi di bawahnya.
- **Safety**: Melindungi konsumen (seperti GUI) dari perubahan drastis di internal JADX. Jika JADX diupdate atau diganti mesin lain, kode di GUI tidak akan pecah karena ia hanya mengenal `dexforge-api`.

### Contoh Isi:
- `dexforge.api.core.DexForgeProject`
- `dexforge.api.model.DexForgeClass`
- `dexforge.api.diagnostic.DexForgeDiagnostic`

---

## 2. dexforge-core (The Engine)

Modul ini adalah "Otot" dari DexForge. Ia bertanggung jawab atas eksekusi berat dan integrasi teknis dengan library pihak ketiga (JADX).

### Karakteristik:
- **Infrastructure Layer**: Tempat di mana JADX Core benar-benar dipanggil.
- **Adapter Bridge**: Menyediakan kelas `Helper` (seperti `JadxNodeHelper`) yang melakukan *type casting* dan manipulasi node JADX untuk digunakan oleh API.
- **Optimization**: Mengelola fitur seperti Disk Cache, Memory Management, dan optimasi performa lainnya yang terlalu teknis untuk diekspos di lapisan API.

### Contoh Isi:
- `dexforge.core.infrastructure.jadx.JadxNodeHelper`
- `dexforge.core.infrastructure.jadx.JadxDecompilerHelper`
- Logika Caching dan Indexing.

---

## Alur Data (Data Flow)

1.  **GUI** meminta kode dari sebuah kelas melalui `DexForgeClass.getCode()`.
2.  **dexforge-api** (lapisan API) menerima permintaan tersebut.
3.  **dexforge-api** memanggil **dexforge-core** melalui `JadxNodeHelper.decompile(delegate)`.
4.  **dexforge-core** melakukan komunikasi dengan **JADX Core**, melakukan casting, dan mengembalikan string kode.
5.  **GUI** menerima string kode tanpa pernah tahu bahwa string tersebut berasal dari JADX.

## Aturan Import (Enforcement)

- **Dilarang keras** melakukan `import jadx.*` di dalam modul `dexforge-api`.
- Jika Anda membutuhkan fungsionalitas baru dari JADX, tambahkan metode baru di `Helper` kelas dalam `dexforge-core`, lalu ekspos melalui `dexforge-api` menggunakan tipe data standar Java atau tipe data DexForge sendiri.

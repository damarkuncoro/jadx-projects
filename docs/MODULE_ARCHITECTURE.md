# Arsitektur Modul: API vs Core vs Engine

Dokumen ini menjelaskan tanggung jawab, batasan, dan hubungan antara modul-modul utama dalam ekosistem DexForge.

---

## Ringkasan Perbandingan

| Fitur | **dexforge-api** | **dexforge-core** | **dexforge-engine-jadx** |
| :--- | :--- | :--- | :--- |
| **Tujuan** | Boundary Publik & Kontrak Domain | Logika Aplikasi & DDD Domain | Adapter Decompiler (JADX) |
| **Ketergantungan JADX** | **Zero** | **Zero** | **High** (Mengelola `jadx-core`) |
| **Konsumen Utama** | GUI, CLI, Plugins | `dexforge-api` | `dexforge-core` (via SPI/Bridge) |
| **Isi Utama** | Model, Persistence Interfaces | Use Cases, Project Management | JADX Bridge, Session Implementation |
| **Prinsip Arsitektur** | API Boundary | Domain Driven Design (DDD) | Adapter Pattern |
| **Stabilitas** | Sangat Stabil | Stabil | Fleksibel (Mengikuti JADX) |

---

## 1. dexforge-api

Modul ini adalah "Wajah" dari DexForge. Ia mendefinisikan *apa* yang bisa dilakukan oleh sistem tanpa peduli *bagaimana* cara melakukannya.

### Karakteristik:
- **Clean Architecture**: Bertindak sebagai lapisan *API Boundary*.
- **Opaque Implementation**: Menyediakan interface dan DTO untuk interaksi eksternal.
- **Safety**: Melindungi konsumen dari perubahan di internal engine.

### Contoh Isi:
- `dexforge.api.core.DexForgeProject`
- `dexforge.api.persistence.DexForgeProjectStore`
- `dexforge.api.model.DexForgeClass`

---

## 2. dexforge-core

Modul ini adalah "Otak" dari DexForge. Ia berisi logika bisnis dan orchestrasi yang tidak bergantung pada decompiler tertentu.

### Karakteristik:
- **JADX-Free**: Tidak boleh ada import `jadx.*`. Semua interaksi dengan engine dilakukan melalui abstraksi.
- **DDD Implementation**: Berisi Aggregate Roots seperti `Project` dan domain services.
- **Application Services**: Mengelola lifecycle dekompilasi, pencarian, dan persistensi.

### Contoh Isi:
- `dexforge.domain.model.project.Project`
- `dexforge.domain.service.ProjectPersistenceService`
- `dexforge.core.application.decompile.DecompileApplicationService`

---

## 3. dexforge-engine-jadx

Modul ini adalah "Otot" khusus untuk JADX. Ia mengimplementasikan interface yang diminta oleh Core menggunakan JADX.

### Karakteristik:
- **Infrastructure Layer**: Tempat di mana JADX Core benar-benar dipanggil.
- **Adapter Bridge**: Melakukan mapping antara model DexForge dan internal JADX.
- **Engine Specific**: Berisi konfigurasi khusus JADX.

### Contoh Isi:
- `dexforge.engine.jadx.infrastructure.JadxDecompilerEngine`
- `dexforge.engine.jadx.infrastructure.JadxSettingsAdapter`

---

## Alur Data (Data Flow)

1.  **GUI** memanggil `DexForgeProject.load()`.
2.  **dexforge-core** (via `DecompileApplicationService`) memulai proses.
3.  **dexforge-core** memanggil engine port (interface).
4.  **dexforge-engine-jadx** (sebagai implementasi) melakukan pemanggilan ke **JADX Core**.
5.  Data dikembalikan ke GUI dalam bentuk model DexForge yang bersih.

## Aturan Import (Enforcement)

- **Dilarang keras** melakukan `import jadx.*` di dalam `dexforge-api` dan `dexforge-core`.
- Dependensi JADX hanya boleh ada di modul `dexforge-engine-jadx` atau module `jadx-*` asli.
- Gunakan `DexForgeProjectStore` dan `ProjectPersistenceService` untuk menangani state project secara engine-agnostic.

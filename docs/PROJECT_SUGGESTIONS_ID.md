# Saran dan Panduan untuk Proyek DexForge

## 0. Frida Integration (Modul Baru)

Proyek DexForge sekarang memiliki modul `jadx-frida-integration` yang mengintegrasikan fungsi runtime hooking dan tracing menggunakan **Frida**! Dengan fitur ini, kamu bisa:
- Generate script Frida langsung dari method yang dipilih di DexForge GUI (klik kanan method → "Generate Frida Hook Script")
- Menggunakan panel Frida di DexForge GUI untuk mengedit dan menjalankan script
- Akses snippet predefined untuk tugas umum seperti bypass SSL pinning, bypass root detection, dll.
- Dukung multi-platform: Android, iOS, Linux, macOS, Windows

### Cara Menggunakan:
1. Decompile APK/app target di DexForge GUI
2. Pilih method yang ingin dihook, klik kanan → pilih "Generate Frida Hook Script"
3. Panel Frida akan terbuka dengan script yang sudah di-generate
4. (Opsional) Pilih snippet predefined dari dropdown
5. Klik "Run Frida Script" untuk menjalankan script pada target process

## 0.1. LSP & JSON-RPC Daemon Mode (Fitur Baru)

Proyek DexForge sekarang mendukung mode **LSP (Language Server Protocol) JSON-RPC Daemon** melalui perintah `lsp` atau `decompiler-daemon`. Fitur ini sangat ramah untuk integrasi dengan VS Code, IntelliJ, atau Android Studio.
*   **Kecepatan Tinggi**: Menghilangkan latensi startup JVM dengan menjaga satu proses JVM tetap aktif di latar belakang.
*   **Fitur Terintegrasi**: Mendukung resolusi definisi (`textDocument/definition`), pelacakan referensi (`textDocument/references`), hover signature (`textDocument/hover`), pencarian simbol fuzzy global (`workspace/symbol`), serta pelaporan warning/error dekompilasi (`diagnostics`).
*   **Pemrosesan Asinkron**: Pemrosesan query asinkron berbasis Thread Pool memastikan antarmuka komunikasi tidak memblokir input/output utama daemon.

## 0.2. Project Persistence & Architecture Cleanup (Update Terbaru)

Kami telah memperkuat arsitektur DexForge untuk mendukung skalabilitas jangka panjang dan independensi dari JADX:
*   **Zero JADX Dependency di API & Core**: Modul `dexforge-api` dan `dexforge-core` sekarang 100% bebas dari dependensi langsung ke JADX. Semua logika engine decompiler dipindahkan ke modul khusus: `dexforge-engine-jadx`.
*   **Project Persistence Layer**: Menambahkan kemampuan untuk menyimpan dan memuat "state" project (rename history, input files, module metadata) secara engine-agnostic menggunakan format JSON.
*   **Clean DDD (Domain Driven Design)**: Memperkenalkan aggregate root `Project` dan domain services yang mengelola lifecycle dekompilasi tanpa tercemar detail infrastruktur.

## 1. Ringkasan Perubahan yang Sudah Dilakukan

Berikut adalah perubahan yang telah diterapkan pada proyek ini:
- **Menghapus Dead Code**: Membersihkan variabel `insnsCount` yang tidak dipakai di `jadx-core/src/main/java/jadx/core/utils/InsnRemover.java`
- **Memperbaiki Masalah Cache GUI**: Mengubah `saveCaches` di `jadx-gui/src/main/java/jadx/gui/cache/manager/CacheManager.java` agar tidak crash ketika tidak bisa menulis file cache
- **Menyiapkan Direktori Aman**: Membuat folder `~/.jadx/config` dan `~/.jadx/cache` untuk menyimpan konfigurasi dan cache JADX tanpa masalah izin
- **Menyetel Environment Variable Permanen**: Menambahkan variabel `JADX_CONFIG_DIR` dan `JADX_CACHE_DIR` di `~/.zshrc`
- **Menambahkan Frida Integration**: Menambahkan modul `jadx-frida-integration` dan panel Frida di GUI
- **Penerapan Clean Architecture & SOLID di jadx-core**: Mendekopel kelas inti `ClassNode` dan presentasi output `CodeGen` melalui antarmuka dinamis `ICodeGenerator`.
- **Implementasi LSP Daemon Mode di jadx-cli**: Menambahkan driver komunikasi JSON-RPC asinkron, router perintah, layanan inti JADX (`DaemonService`), dan layanan pemetaan protokol LSP (`LspService`).
- **Architecure Cleanup & Project Persistence**: Relokasi dependensi engine ke `dexforge-engine-jadx` dan implementasi `ProjectPersistenceService` yang engine-agnostic.

## 2. Cara Menjalankan DexForge GUI

### Cara Cepat (Sudah Dikonfigurasi)
```bash
./gradlew :jadx-gui:run --no-daemon
```

### Catatan Penting
Pastikan variabel environment di `~/.zshrc` sudah aktif. Jika baru ditambahkan, jalankan:
```bash
source ~/.zshrc
```

## 3. Masalah yang Biasa Ditemukan dan Solusinya

### 3.1 Error "The project cannot be built until build path errors are resolved" di IDE
**Penyebab**: Cache Gradle yang dihapus atau IDE belum sinkron
**Solusi**:
1. Tutup IDE
2. Hapus folder `.idea` dan file `*.iml` di root proyek
3. Buka kembali proyek di IDE dan tunggu proses import Gradle selesai
4. Atau klik "Reload All Gradle Projects" di IDE

### 3.2 Error "Failed to write caches file" di GUI
**Penyebab**: Izin menulis direktori konfigurasi default macOS (`~/Library/Application Support/io.github.skylot.jadx`)
**Solusi**: Sudah diperbaiki dengan menggunakan direktori baru di `~/.jadx/config` dan `~/.jadx/cache` (dengan variabel environment)

### 3.3 Warning "One or more cycles were detected in the build path"
**Penyebab**: Dependensi siklik di modul proyek (contoh: `jadx-core` ↔ `jadx-dex-input`)
**Solusi**: Peringatan ini **aman**, Gradle masih bisa menangani build normal. Tidak perlu diperbaiki kecuali ingin merubah arsitektur proyek besar-besaran.

## 4. Saran untuk Pengembangan Selanjutnya

### 4.1 Menghindari Menghapus Cache Gradle Secara Manual
Hanya hapus cache Gradle (`~/.gradle/caches`) jika ada masalah serius dengan dependensi. Menghapusnya akan membuat Gradle harus mengunduh ulang semua library, yang memakan waktu.

### 4.2 Menggunakan Wrapper Gradle
Selalu gunakan `./gradlew` (wrapper Gradle) yang sudah disediakan di proyek, bukan Gradle yang diinstal secara global. Ini memastikan semua orang menggunakan versi Gradle yang sama.

### 4.3 Struktur Direktori Konfigurasi dan Cache
Direktori baru:
- Konfigurasi: `~/.jadx/config`
- Cache: `~/.jadx/cache`

Ini lebih aman dan mudah diakses dibanding direktori default di `~/Library/Application Support`.

### 4.4 Menjalankan Test
Untuk menjalankan test (opsional):
```bash
./gradlew test --no-daemon
```

Catatan: Test bisa memakan waktu cukup lama.

## 5. Referensi
- Dokumentasi utama DexForge: [README.md](../README.md)
- Panduan kontribusi: [CONTRIBUTING.md](../CONTRIBUTING.md)

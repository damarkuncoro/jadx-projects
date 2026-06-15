# Saran dan Panduan untuk Proyek JADX

## 1. Ringkasan Perubahan yang Sudah Dilakukan

Berikut adalah perubahan yang telah diterapkan pada proyek ini:
- **Menghapus Dead Code**: Membersihkan variabel `insnsCount` yang tidak dipakai di `jadx-core/src/main/java/jadx/core/utils/InsnRemover.java`
- **Memperbaiki Masalah Cache GUI**: Mengubah `saveCaches` di `jadx-gui/src/main/java/jadx/gui/cache/manager/CacheManager.java` agar tidak crash ketika tidak bisa menulis file cache
- **Menyiapkan Direktori Aman**: Membuat folder `~/.jadx/config` dan `~/.jadx/cache` untuk menyimpan konfigurasi dan cache JADX tanpa masalah izin
- **Menyetel Environment Variable Permanen**: Menambahkan variabel `JADX_CONFIG_DIR` dan `JADX_CACHE_DIR` di `~/.zshrc`

## 2. Cara Menjalankan JADX GUI

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
- Dokumentasi utama JADX: [README.md](../README.md)
- Panduan kontribusi: [CONTRIBUTING.md](../CONTRIBUTING.md)

# Project Requirements

Dokumen ini merinci kebutuhan yang dibutuhkan untuk membangun dan menjalankan proyek ini.

## Prasyarat sistem

- Git
- JDK 17 atau lebih baru
  - Proyek menggunakan Gradle wrapper, tetapi build dan runtime target disarankan memakai Java 17+.
  - Minimum Java 11 diperlukan oleh konfigurasi Gradle, tetapi untuk menjalankan `jadx-gui` dan fitur terbaru sebaiknya gunakan Java 17 atau lebih baru.
- Memori yang cukup untuk kompilasi besar (disarankan 8GB+ tersedia)

## Toolchain dan dependensi

- `./gradlew` sudah tersedia di repositori sebagai Gradle wrapper
- Gradle wrapper akan mengunduh versi Gradle yang tepat secara otomatis
- Tidak perlu menginstal Gradle secara global jika menggunakan wrapper

## Kebutuhan tambahan untuk `jadx-gui`

- JDK 17+ pada `JAVA_HOME`
- `adb` / Android SDK Platform Tools hanya diperlukan untuk fitur Device Explorer
  - Jika ingin menggunakan `jadx-gui` tanpa Device Explorer, `adb` bersifat opsional
  - Jika ingin menjalankan Device Explorer, siapkan:
    - `ANDROID_HOME` atau `ANDROID_SDK_ROOT` menunjuk ke SDK Android
    - `adb` di `PATH`, atau `platform-tools/adb` dapat ditemukan otomatis
    - Perangkat Android dengan USB debugging aktif

## Variabel lingkungan yang berguna

- `JAVA_HOME` → lokasi JDK
- `ANDROID_HOME` atau `ANDROID_SDK_ROOT` → lokasi Android SDK jika menggunakan ADB
- `JADX_CONFIG_DIR` → lokasi konfigurasi khusus (opsional)
- `JADX_CACHE_DIR` → cache custom (opsional)
- `JADX_TMP_DIR` → direktori temp custom (opsional)

## Langkah build dan jalankan

### Build dari source

```bash
./gradlew build
```

### Jalankan GUI dari source

```bash
./gradlew :jadx-gui:run
```

### Distribusi / paket

```bash
./gradlew dist
```

## Rilis binary-only

Untuk pengguna yang hanya butuh produk jadi tanpa source build:

- Jalankan `./gradlew dist` untuk membuat distribusi build.
- Kumpulkan artifact hasil build dari direktori `build/jadx/` atau `jadx-gui/build/libs/`.
- Buat package seperti ZIP/TAR yang berisi:
  - `bin/` (scripts/executable)
  - `lib/` (library JARs)
  - `README.md` atau `LICENSE`
- Upload package tersebut sebagai asset di GitHub Releases.
- Jangan sertakan folder `src/` atau file sumber di binary-only release asset.

Pengguna bisa mengunduh release asset tersebut dan langsung menjalankan produk jadi.

### Release helper script

Repositori menyediakan helper sederhana di `scripts/release.sh` untuk membuat GitHub Release dari binary artifact:

```bash
./gradlew dist
./scripts/release.sh v1.0.0
```

Opsional:

```bash
./scripts/release.sh v1.0.0 --artifact build/jadx/jadx-v1.0.0.zip --notes-file release-notes.md
```

## GitHub Release Workflow

1. Pastikan branch `main` sudah up to date dan semua perubahan sudah di-commit.
2. Buat tag rilis yang jelas, misalnya `v1.0.0`:
   ```bash
git tag -a v1.0.0 -m "Release v1.0.0"
git push origin v1.0.0
```
3. Siapkan artifact binary:
   - `./gradlew dist`
   - Paketkan output `build/jadx/` atau `jadx-gui/build/libs/` menjadi file `.zip` / `.tar.gz`
4. Buat release di GitHub Releases melalui UI atau GitHub CLI:
   ```bash
gh release create v1.0.0 build/jadx/release.zip --title "v1.0.0" --notes "Binary-only release for end users."
```
5. Cantumkan di deskripsi bahwa source code tetap tersedia di repo, tetapi asset yang diunduh adalah produk jadi.

### Windows

- Gunakan `gradlew.bat` sebagai pengganti `./gradlew`

## Hal yang perlu diperhatikan

- Proyek ini adalah fork lokal dari `skylot/jadx`
- Semua perintah Gradle sebaiknya dijalankan dari direktori root repositori
- Jika menggunakan fitur Device Explorer, pastikan ADB bisa terhubung ke perangkat Android
- Jika terjadi error build, periksa versi JDK dan variabel lingkungan `JAVA_HOME`

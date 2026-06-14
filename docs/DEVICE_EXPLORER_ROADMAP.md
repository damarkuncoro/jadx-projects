# JADX Device Explorer Roadmap

## Ringkasan

JADX Device Explorer adalah fitur tambahan untuk `jadx-gui` yang memungkinkan pengguna mengambil APK langsung dari perangkat Android melalui ADB, termasuk `base.apk` dan semua split APK, lalu membukanya atau mendecompile otomatis dengan JADX.

Target alur utama:

```text
Detect Android device via ADB
  -> list installed packages
  -> user selects a package
  -> resolve base.apk and split APK paths
  -> pull APK files into a workspace
  -> open or decompile with JADX
```

Fitur ini sebaiknya dimulai sebagai helper eksternal yang kecil dan stabil, lalu dipindahkan ke `jadx-gui` setelah perilaku ADB, folder output, dan error handling sudah matang.

## Tujuan Produk

- Mempercepat workflow reverse engineering dari perangkat fisik atau emulator.
- Menghindari kesalahan umum saat hanya menarik `base.apk` tanpa split APK.
- Menyediakan workspace rapi per package untuk APK, hasil decompile, metadata, dan laporan.
- Memberikan UI yang mudah dipakai untuk memilih device, package, split APK, dan aksi decompile.
- Menangani kondisi Android modern seperti multi-user, work profile, clone profile, dan permission error.

## Non-Goals Awal

- Tidak melakukan bypass permission Android atau root-only extraction.
- Tidak mengambil data aplikasi pribadi dari `/data/data`.
- Tidak menggantikan Android Studio Device Explorer.
- Tidak langsung membangun fitur scanner kompleks sebelum puller dan decompiler stabil.

## Nama Fitur

Nama utama yang disarankan:

```text
JADX Device Explorer
```

Alternatif:

```text
Device APK Explorer
Open from Android Device
JADX Pull & Rescue
```

Untuk UI, entry point paling natural:

```text
File -> Open from Android Device...
```

## Arsitektur Awal

Komponen inti:

```text
ADB Connector
  - detect adb executable
  - run adb commands
  - parse stdout and stderr
  - expose structured errors

Device Manager
  - list connected devices
  - detect unauthorized/offline devices
  - select target device
  - refresh device state

Package Browser
  - list packages
  - filter user apps/system apps/all apps
  - search package name
  - resolve package APK paths
  - optionally resolve labels/icons later

APK Puller
  - pull base.apk
  - pull split APK files
  - verify file existence and file size
  - save pull metadata

Decompile Runner
  - open pulled APK files in jadx-gui
  - run decompile task with selected APK set
  - store output in package workspace
  - show errors and warnings

Workspace Manager
  - create stable package folder structure
  - write metadata
  - prevent accidental overwrite
```

## Workspace Layout

Setiap package dibuat sebagai project kecil:

```text
workspace/
└── id.net.cakramedia.attendance/
    ├── apks/
    │   ├── base.apk
    │   ├── split_config.arm64_v8a.apk
    │   ├── split_config.xxxhdpi.apk
    │   └── split_config.id.apk
    ├── jadx-output/
    ├── apktool-output/
    ├── reports/
    │   ├── pull-report.json
    │   └── decompile-report.json
    └── notes.md
```

Metadata minimum di `pull-report.json`:

```json
{
  "packageName": "id.net.cakramedia.attendance",
  "deviceSerial": "R58M123456A",
  "androidUser": 0,
  "pulledAt": "2026-06-14T00:00:00Z",
  "apkFiles": [
    {
      "remotePath": "/data/app/example/base.apk",
      "localPath": "apks/base.apk",
      "sizeBytes": 12345678,
      "type": "base"
    }
  ]
}
```

## ADB Commands

List device:

```bash
adb devices -l
```

List package:

```bash
adb shell pm list packages
adb shell pm list packages -3
adb shell pm list packages -s
adb shell pm list packages -f -3
```

Resolve APK paths:

```bash
adb shell pm path id.net.cakramedia.attendance
```

Contoh output:

```text
package:/data/app/~~abc123/id.net.cakramedia.attendance-xyz/base.apk
package:/data/app/~~abc123/id.net.cakramedia.attendance-xyz/split_config.arm64_v8a.apk
package:/data/app/~~abc123/id.net.cakramedia.attendance-xyz/split_config.xxxhdpi.apk
package:/data/app/~~abc123/id.net.cakramedia.attendance-xyz/split_config.id.apk
```

Pull APK:

```bash
adb pull <remote-path> <local-path>
```

Decompile:

```bash
jadx --show-bad-code \
  --no-inline-methods \
  --no-inline-kotlin-lambda \
  --no-replace-consts \
  -d jadx-output \
  apks/*.apk
```

## Multi-User dan Work Profile

Android device bisa punya beberapa user/profile:

```bash
adb shell pm list users
```

Contoh:

```text
UserInfo{0:Owner:13} running
UserInfo{999:Clone:30} running
```

Package per user:

```bash
adb shell pm list packages --user 0
adb shell pm list packages --user 999
```

Aturan implementasi:

- Default ke user `0`.
- Tampilkan pilihan Android user jika `pm list users` berhasil.
- Jika user tertentu ditolak, tampilkan warning dan lanjutkan dengan user yang masih bisa dibaca.
- Error seperti `Shell does not have permission to access user 999` harus dianggap recoverable, bukan fatal.
- Jangan mencoba akses path private app data.

## UI Konsep

Dialog awal:

```text
Open from Android Device

Connected Device
[ R58M123456A                       v ] [ Refresh ]

Android User
[ 0: Owner                          v ]

Search package
[ attendance                                      ]

Filters
(*) User apps only   ( ) System apps   ( ) All apps

Packages
  id.net.cakramedia.attendance
  com.whatsapp
  com.google.android.gms

APK Splits
  [x] base.apk
  [x] split_config.arm64_v8a.apk
  [x] split_config.xxxhdpi.apk
  [x] split_config.id.apk

[ Pull APK ] [ Pull + Open ] [ Pull + Decompile ]
```

Status yang perlu terlihat:

- ADB executable tidak ditemukan.
- Tidak ada device.
- Device unauthorized.
- Device offline.
- Package list kosong.
- APK path gagal di-resolve.
- Pull sebagian gagal.
- Decompile selesai dengan warning.

## Roadmap Milestone

### v0.1 - External Helper CLI [COMPLETED]

Tujuan: membuktikan workflow ADB dan split APK tanpa menyentuh GUI besar.
Status: **Selesai (14 Juni 2026)** - Service layer, unit test, CLI wrapper, dan verifikasi manual di perangkat Infinix X6833B telah berhasil diselesaikan.

Fitur:

- `list-devices`
- `list-packages`
- `list-users`
- `paths <package>`
- `pull <package>`
- `pull-and-decompile <package>`
- Output workspace per package.
- Metadata `pull-report.json`.

Acceptance criteria:

- Bisa menarik semua APK dari package yang punya split.
- Bisa berjalan di emulator dan minimal satu device fisik.
- Error unauthorized/offline ditampilkan jelas.
- Tidak gagal total saat user profile tertentu tidak bisa diakses.

### v0.2 - Integrasi Minimal ke JADX GUI [COMPLETED]

Tujuan: pengguna bisa membuka APK dari device langsung dari GUI.
Status: **Selesai (14 Juni 2026)** - Penambahan `OPEN_DEVICE_EXPLORER` action, lokalisasi, menu, toolbar, dialog Swing `DeviceExplorerDialog` dan penarikan APK latar belakang selesai.

Fitur:

- Menu `File -> Open from Android Device...`.
- Dialog device selector.
- Package browser dengan search.
- Tombol `Pull APK`.
- Tombol `Pull + Open`.
- Progress task memakai background job existing di `jadx-gui`.

Acceptance criteria:

- UI tidak freeze selama ADB command berjalan.
- Pull progress dan error tampil di dialog.
- APK yang sudah di-pull bisa langsung dibuka sebagai input JADX.
- Folder workspace bisa dipilih atau memakai default setting.

### v0.3 - Split APK Inspector [COMPLETED]

Tujuan: pengguna bisa memahami isi split sebelum decompile.
Status: **Selesai (14 Juni 2026)** - Klasifikasi kategori split APK (base, ABI, density, dll) dan peringatan konfirmasi jika split tidak lengkap atau base APK tidak terpilih selesai.

Fitur:

- Klasifikasi split:
  - base
  - ABI
  - density
  - language
  - feature module
- Checkbox untuk include/exclude split.
- Warning jika `base.apk` tidak terpilih.
- Warning jika kombinasi split berpotensi tidak lengkap.

Acceptance criteria:

- Nama split umum seperti `split_config.arm64_v8a.apk`, `split_config.xxxhdpi.apk`, dan `split_config.id.apk` diklasifikasikan benar.
- User tetap bisa memilih semua split dengan satu klik.
- Default pilihan adalah semua APK dari `pm path`.

### v0.4 - Decompile Runner dan Reports [COMPLETED]

Tujuan: workflow pull sampai hasil decompile menjadi satu langkah.
Status: **Selesai (14 Juni 2026)** - Implementasi `DeviceExplorerExportTask` kustom, integrasi decompile otomatis, durasi pencatatan, dan pembuatan berkas laporan `reports/decompile-report.json` selesai.

Fitur:

- `Pull + Decompile`.
- Output ke `jadx-output/`.
- Save `decompile-report.json`.
- Open result otomatis setelah selesai.
- Opsi decompile aman untuk APK obfuscated.

Acceptance criteria:

- Bisa menjalankan decompile dari APK set hasil pull.
- Error decompile tidak menghapus APK yang sudah di-pull.
- Report menyimpan command, durasi, input APK, output path, dan error summary.

### v0.5 - ELF & Binary XML Layout Viewer [COMPLETED]

Tujuan: memberikan kemampuan membuka berkas biner ELF dan berkas layout XML biner Android di JADX GUI secara mulus dan hemat memori.
Status: **Selesai (14 Juni 2026)** - Parsing header ELF 32/64-bit hemat memori (64 byte), tampilan tab Text & Hex untuk `.so`, deteksi XML biner tanpa ekstensi standar via signature magic byte (`0x03 0x00 0x08 0x00`), dan parsing ke teks XML bersih dengan ikon & penyorot sintaksis yang sesuai selesai.

Fitur:

- Deteksi otomatis berkas ELF (`\x7FELF`) dan XML biner Android.
- Tampilan tab **Text** (informasi header ELF) dan tab **Hex** (byte mentah) untuk berkas `.so`.
- Parser header ELF yang dioptimalkan hanya membaca 64 byte pertama berkas untuk meminimalkan beban memori.
- Konversi otomatis XML biner layout/drawable (sekalipun tanpa ekstensi berkas standar) ke bentuk teks XML bersih melalui `BinaryXMLParser` internal.
- Dukungan penyorotan sintaksis XML dan ikon XML yang tepat di pohon GUI.

Acceptance criteria:

- Berkas `.so` menampilkan tab Text dengan informasi arsitektur, endianness, entry point, dll.
- Berkas layout biner (sekalipun biner mentah/tanpa ekstensi) sukses didecode ke XML teks saat diklik.
- Memori tidak membengkak saat membuka berkas ELF berukuran besar.

### v0.6 - Reverse Engineering Assistant

Tujuan: menambah analisis otomatis setelah decompile.

Fitur:

- Endpoint scanner.
- Firebase config scanner.
- Base64/string scanner.
- Crypto usage scanner.
- Obfuscation summary.
- Failed method report.

Acceptance criteria:

- Scanner berjalan opsional.
- Hasil scanner masuk ke `reports/`.
- UI bisa membuka ringkasan report tanpa memblokir decompile.

## Detail Implementasi yang Disarankan

### Service Layer

Mulai dengan service yang tidak bergantung pada Swing:

```text
jadx.gui.device.adb.AdbService
jadx.gui.device.adb.AdbDevice
jadx.gui.device.adb.AdbPackage
jadx.gui.device.adb.ApkPath
jadx.gui.device.adb.AdbException
```

Alasan:

- Mudah dites tanpa UI.
- Bisa dipakai CLI helper dan GUI.
- Error ADB bisa distandarkan sebelum masuk dialog.

### Background Task

Semua command ADB harus berjalan di background task:

```text
list devices
list packages
resolve APK paths
pull APK
decompile
```

UI Swing tidak boleh menunggu process ADB secara blocking di EDT.

### Parsing Output

Parser perlu toleran terhadap output ADB yang bervariasi:

- Trim empty lines.
- Abaikan header `List of devices attached`.
- Pisahkan status device: `device`, `offline`, `unauthorized`.
- Untuk `pm path`, hanya terima line dengan prefix `package:`.
- Untuk `pm list packages`, hapus prefix `package:`.
- Untuk `pm list packages -f`, parse path dan package name secara hati-hati.

### ADB Discovery

Urutan pencarian `adb`:

1. Path dari setting user.
2. `ANDROID_HOME/platform-tools/adb`.
3. `ANDROID_SDK_ROOT/platform-tools/adb`.
4. `adb` dari `PATH`.

Jika tidak ditemukan, tampilkan pesan dengan instruksi singkat untuk memasang Android SDK Platform Tools.

## Risiko dan Mitigasi

| Risiko | Dampak | Mitigasi |
| --- | --- | --- |
| Device unauthorized | User tidak bisa list package | Tampilkan instruksi authorize RSA prompt di device |
| Work profile ditolak | Sebagian package tidak terlihat | Fallback ke user 0 dan tampilkan warning |
| Split APK tidak lengkap | Decompile/resource salah | Default pull semua hasil `pm path` |
| ADB command hang | UI freeze | Background task + timeout + cancel |
| Output path overwrite | Data lama hilang | Folder timestamp atau prompt overwrite |
| APK besar | Pull lama | Progress log dan cancel |
| Platform berbeda | Path adb beda | ADB discovery lintas OS |

## Test Plan

Unit tests:

- Parse `adb devices -l`.
- Parse `pm list packages`.
- Parse `pm list packages -f`.
- Parse `pm path`.
- Classify split APK names.
- Build workspace paths safely.

Integration/manual tests:

- Emulator tanpa split APK.
- Emulator dengan APK split.
- Device fisik authorized.
- Device offline.
- Device unauthorized.
- Package tidak ditemukan.
- Work profile/user `999` permission denied.
- Pull ulang package yang sama.

## Contoh Helper CLI v0.1

Script awal untuk validasi manual:

```bash
#!/bin/bash

PACKAGE="$1"

if [ -z "$PACKAGE" ]; then
  echo "Usage: ./pull-apk.sh <package-name>"
  exit 1
fi

OUT_DIR="workspace/$PACKAGE/apks"
mkdir -p "$OUT_DIR"

echo "[*] Getting APK paths for $PACKAGE..."

adb shell pm path "$PACKAGE" | while read -r line; do
  APK_PATH="${line#package:}"
  FILE_NAME="$(basename "$APK_PATH")"

  echo "[*] Pulling $FILE_NAME..."
  adb pull "$APK_PATH" "$OUT_DIR/$FILE_NAME"
done

echo "[*] Done."
echo "[*] Files saved to $OUT_DIR"
```

Pemakaian:

```bash
chmod +x pull-apk.sh
./pull-apk.sh id.net.cakramedia.attendance
```

Decompile:

```bash
jadx --show-bad-code \
  -d workspace/id.net.cakramedia.attendance/jadx-output \
  workspace/id.net.cakramedia.attendance/apks/*.apk
```

## Definition of Done

Fitur dianggap siap untuk dipakai harian jika:

- Pull base + split APK berhasil dari device/emulator.
- GUI tidak freeze selama operasi.
- Error ADB terbaca manusia.
- Workspace tersimpan rapi.
- Hasil pull bisa langsung dibuka di JADX.
- Ada test untuk parser dan split classifier.
- Ada dokumentasi user singkat di README atau docs lanjutan.


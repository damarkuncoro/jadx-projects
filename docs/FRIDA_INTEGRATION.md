# DexForge Frida Integration

DexForge menyediakan integrasi penuh dengan [Frida](https://frida.re/) untuk dynamic instrumentation dan runtime hooking pada aplikasi Android. Fitur ini memungkinkan security researcher dan reverse engineer untuk:

- Menghasilkan script Frida langsung dari dekompilasi kode Java
- Menjalankan script Frida pada perangkat Android yang terhubung
- Mem-bypass SSL pinning, root detection, dan proteksi umum lainnya
- Melakukan hooking method, monitoring parameter, dan memodifikasi return value
- Mencekati APK dengan frida-gadget untuk perangkat non-rooted

## Arsitektur

```
┌─────────────────────────────────────────────────────────────┐
│                    DexForge GUI / CLI                        │
├─────────────────────────────────────────────────────────────┤
│  FridaPanel (GUI) / FridaProcessExecutor (CLI)              │
│           │                                                 │
│           ▼                                                 │
│  FridaScriptManager ──► IFridaScriptGenerator               │
│           │              (FridaScriptGenerator)              │
│           ▼                                                 │
│  FridaSnippetRegistry ──► IFridaSnippet                     │
│           │              (FridaSnippets enum)                │
│           │                                                 │
│           ▼                                                 │
│  FridaDownloader ──► Download frida-server/gadget           │
│           │                                                 │
│           ▼                                                 │
│  FridaApkPatcher ──► Inject frida-gadget into APK           │
│           │                                                 │
│           ▼                                                 │
│  FridaServerManager ──► Auto-start frida-server on device   │
└─────────────────────────────────────────────────────────────┘
```

## Komponen Utama

### 1. FridaScriptGenerator

Menghasilkan script Frida dari dekompilasi kode Java.

**Location:** `dexforge-plugins/dexforge-frida-integration/src/main/java/dexforge/frida/FridaScriptGenerator.java`

**Methods:**
- `generateMethodHook(JavaMethod)` - Generate hook untuk method tertentu
- `generateMethodSnippet(JavaMethod, JavaClass)` - Generate snippet untuk method
- `generateFieldSnippet(JavaField, JavaClass)` - Generate snippet untuk field
- `generateClassAllMethodSnippet(JavaClass, List<JavaMethod>)` - Generate hook untuk semua method dalam class

### 2. FridaSnippets

Kumpulan script Frida pre-built untuk keperluan umum.

**Location:** `dexforge-plugins/dexforge-frida-integration/src/main/java/dexforge/frida/FridaSnippets.java`

**Built-in Snippets:**
| Snippet | Deskripsi |
|---------|-----------|
| `BYPASS_SSL_PINNING` | Mem-bypass SSL certificate pinning |
| `BYPASS_ROOT_DETECTION` | Mem-bypass deteksi rooted device |
| `LOG_ALL_METHODS_IN_CLASS` | Log semua pemanggilan method dalam class |
| `DUMP_SHARED_PREFERENCES` | Dump isi SharedPreferences |
| `LOG_OKHTTP_REQUESTS` | Log semua HTTP request dari OkHttp |

### 3. FridaPanel (GUI)

Panel GUI untuk menjalankan dan mengelola script Frida.

**Location:** `jadx-gui/src/main/java/jadx/gui/frida/FridaPanel.java`

**Fitur:**
- Editor script dengan syntax highlighting
- Auto-completion untuk Frida API
- Snippet management (tambah, edit, hapus custom snippets)
- Log output real-time
- Target selection (package name / process name)

### 4. FridaApkPatcher

Mencekati APK dengan frida-gadget untuk perangkat non-rooted.

**Location:** `jadx-gui/src/main/java/jadx/gui/frida/FridaApkPatcher.java`

**Proses:**
1. Decompile APK menggunakan apktool
2. Inject kode native untuk load `libfrida-gadget.so`
3. Copy frida-gadget ke direktori `lib/`
4. Rebuild dan sign APK
5. Install APK yang sudah dicekati

### 5. FridaServerManager

Mengelola frida-server pada perangkat Android.

**Location:** `jadx-gui/src/main/java/jadx/gui/frida/FridaServerManager.java`

**Fitur:**
- Auto-download frida-server sesuai arsitektur device
- Auto-push ke `/data/local/tmp/`
- Auto-start frida-server (hanya untuk rooted devices)
- Deteksi frida-server yang sudah berjalan

## Penggunaan

### Dari GUI (JadxGUI)

1. Buka APK/DEX yang ingin dianalisis
2. Klik kanan pada method/field yang ingin di-hook
3. Pilih **"Generate Frida Hook Script"**
4. Script akan muncul di Frida Panel
5. Masukkan target package name
6. Klik **"Run Frida Script"**

### Dari CLI

```bash
# Jalankan script Frida pada target
dexforge frida run --target com.example.app --script hook.js

# Generate hook dari dekompilasi
dexforge frida generate --class com.example.MainActivity --method onCreate

# Patch APK dengan frida-gadget
dexforge frida patch --input app.apk --output patched.apk --arch arm64-v8a
```

### Contoh Script Frida yang Dihasilkan

```javascript
Java.perform(function() {
  var TargetClass = Java.use("com.example.MainActivity");

  TargetClass["onCreate"].implementation = function(arg0, arg1) {
    console.log("[*] com.example.MainActivity.onCreate called!");
    console.log("    Arg0: " + arg0);
    console.log("    Arg1: " + arg1);
    var result = this["onCreate"].apply(this, arguments);
    console.log("    Return: " + result);
    return result;
  };
});
```

## Prasyarat

### 1. Frida Terinstal

```bash
# Install Frida tools
pip install frida-tools

# Verifikasi instalasi
frida --version
```

### 2. Perangkat Android

- **Rooted device:** Bisa langsung menjalankan frida-server
- **Non-rooted device:** Harus menggunakan APK yang sudah dicekati dengan frida-gadget

### 3. ADB

```bash
# Pastikan ADB terinstal dan device terdeteksi
adb devices
```

## Workflow: Non-Rooted Device

Untuk perangkat yang tidak di-root, ikuti langkah ini:

### 1. Patch APK dengan Frida Gadget

```bash
# Menggunakan DexForge GUI
# 1. Buka APK di DexForge
# 2. Device Explorer → Pilih device
# 3. Klik "Patch with Frida Gadget"
# 4. Pilih arsitektur (arm64-v8a, armeabi-v7a, dll.)
# 5. Tunggu proses selesai
```

### 2. Install APK yang Sudah Dipatch

```bash
# Uninstall versi original (jika ada)
adb uninstall com.example.app

# Install versi yang dipatch
adb install patched-app.apk
```

### 3. Jalankan Frida Script

```bash
# Buka aplikasi di device
adb shell am start -n com.example.app/.MainActivity

# Jalankan script Frida
frida -U -f com.example.app -l hook.js --no-pause
```

## Workflow: Rooted Device

Untuk perangkat yang sudah di-root:

### 1. Auto-start Frida Server

DexForge GUI akan otomatis:
- Mendownload frida-server sesuai arsitektur device
- Push ke `/data/local/tmp/`
- Start frida-server di background

### 2. Jalankan Script

```bash
# Script bisa langsung dijalankan dari GUI
# Atau via CLI:
dexforge frida run --target com.example.app --script hook.js
```

## Custom Snippets

DexForge memungkinkan menambahkan custom Frida snippets:

### Dari GUI

1. Buka Frida Panel
2. Klik **"Manage Snippets"**
3. Klik **"Add"** untuk snippet baru
4. Masukkan nama dan script
5. Klik **"Save"**

### Dari File Konfigurasi

Custom snippets disimpan di `~/.config/dexforge/settings.json`:

```json
{
  "customFridaSnippets": [
    {
      "name": "My Custom Hook",
      "script": "Java.perform(function() {\n  // Your script here\n});"
    }
  ]
}
```

## Integrasi dengan LSP Daemon

Frida integration juga bisa diakses via LSP daemon untuk IDE automation:

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "dexforge/generateFridaHook",
  "params": {
    "class": "com.example.MainActivity",
    "method": "onCreate",
    "type": "method"
  }
}
```

## Troubleshooting

### Frida Server Tidak Bisa Start

**Gejala:** `[ERROR] Failed to start frida-server`

**Solusi:**
1. Pastikan device rooted (untuk auto-start)
2. Atau gunakan APK yang sudah dipatch dengan frida-gadget
3. Manual start: `adb shell su -c /data/local/tmp/frida-server &`

### Script Tidak Menempel

**Gejala:** Script dijalankan tapi tidak ada output

**Solusi:**
1. Pastikan package name benar
2. Cek apakah class sudah di-load: `Java.choose("com.example.app", {onMatch: function(inst){}, onComplete: function(){}});`
3. Pastikan tidak ada anti-tamper yang mendeteksi Frida

### APK Patch Gagal

**Gejala:** Error saat patch APK

**Solusi:**
1. Pastikan apktool terinstal: `apktool --version`
2. Pastikan APK tidak di-protect dengan anti-reversing
3. Coba dengan APK yang lebih sederhana terlebih dahulu

## Keamanan dan Etika

> **Peringatan:** Frida integration dirancang untuk tujuan security research, debugging, dan recovery aplikasi yang Anda miliki atau memiliki otorisasi. Pengguna bertanggung jawab penuh untuk memastikan kepatuhan terhadap hukum dan regulasi lokal.

## Referensi

- [Frida Documentation](https://frida.re/docs/home/)
- [Frida API Reference](https://frida.re/docs/javascript-api/)
- [DexForge LSP Daemon API](LSP_DAEMON_API.md)

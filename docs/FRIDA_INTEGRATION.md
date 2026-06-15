# Frida Integration Guide

## Overview

Proyek JADX memiliki integrasi Frida yang memungkinkan kamu untuk:
- Menghasilkan script Frida hook secara otomatis dari method yang didecompile
- Mengedit dan menjalankan script Frida langsung dari JADX GUI
- Menggunakan snippet predefined untuk tugas umum seperti bypass SSL pinning, bypass root detection, dll.

## Components

### 1. jadx-frida-integration Module
Modul ini berisi:
- `FridaScriptGenerator`: Kelas untuk menghasilkan script Frida hook dari `JavaMethod`
- `FridaSnippets`: Kumpulan snippet Frida predefined untuk tugas umum

### 2. jadx-gui Integration
- `FridaPanel`: Panel GUI untuk mengedit dan menjalankan script Frida
- `FridaAction`: Aksi untuk menghasilkan dan menyalin snippet Frida dari pohon kode
- Integration di `MainWindow`: Menu untuk menampilkan/sembunyikan panel Frida, dan aksi klik kanan di area kode untuk menghasilkan script

## Usage

### Generate Script from Decompiled Method
1. Buka APK/app target di JADX GUI
2. Navigasi ke method yang ingin dihook di pohon kode atau area kode
3. Klik kanan pada method tersebut
4. Pilih "Generate Frida Hook Script" (nama aksi mungkin berbeda tergantung konfigurasi)
5. Panel Frida akan terbuka dengan script yang sudah di-generate

### Using Predefined Snippets
1. Buka panel Frida (dengan menu atau aksi di atas)
2. Pilih snippet dari dropdown "Predefined Snippets"
3. Script akan dimuat ke editor
4. Edit sesuai kebutuhan
5. Klik "Run Frida Script" untuk menjalankan

### Running Script
1. Pastikan Frida sudah terinstal di sistem kamu (`pip install frida-tools`)
2. Pastikan perangkat Android terhubung (jika target adalah Android) dan USB debugging aktif
3. Klik "Run Frida Script"
4. Masukkan nama package/process target di dialog yang muncul
5. Output Frida akan ditampilkan di log panel

## Predefined Snippets
Berikut adalah snippet yang tersedia:
- **Bypass SSL Pinning**: Melewati verifikasi SSL pinning di Android
- **Bypass Root Detection**: Melewati deteksi root di aplikasi
- **Log All Methods in Class**: Mencatat semua pemanggilan method di kelas tertentu
- **Dump SharedPreferences**: Membaca dan mencatat semua nilai di SharedPreferences
- **Log OkHttp Requests**: Mencatat semua permintaan HTTP yang dibuat dengan OkHttp

## Building
Untuk membangun proyek dengan Frida integration:
```bash
./gradlew dist
```
Ini akan menghasilkan distribusi di `build/jadx/`

## Notes
- Pastikan Frida dan frida-tools sudah terinstal di sistem kamu
- Untuk Android, pastikan `adb` dapat terhubung ke perangkat dan USB debugging aktif
- Script yang di-generate adalah dasar dan bisa diedit sesuai kebutuhan

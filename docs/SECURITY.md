# DexForge Security Guidelines

## Overview

DexForge dirancang untuk tujuan security research, debugging, dan recovery aplikasi yang Anda miliki atau memiliki otorisasi untuk menganalisis. Dokumentasi ini menjelaskan praktik keamanan yang harus diikuti saat menggunakan fitur-fitur DexForge, khususnya Frida integration.

## Legal & Ethical Boundaries

### ✅ Penggunaan yang Diizinkan

- Security research pada aplikasi yang Anda kembangkan
- Debugging aplikasi milik Anda sendiri atau tim Anda
- Malware analysis dalam lingkungan yang terkontrol
- Educational purposes di lingkungan lab
- Recovery data dari aplikasi yang Anda miliki
- Penetration testing dengan written authorization

### ❌ Penggunaan yang Dilarang

- Reverse engineering aplikasi berbayar tanpa izin
- Bypass DRM atau protection mechanisms untuk keperluan ilegal
- Mengekstrak atau mencuri data dari aplikasi pihak ketiga
- Memodifikasi aplikasi untuk tujuan penipuan atau kerusakan
- Melanggar Terms of Service dari aplikasi atau platform

## Frida Integration Security

### 1. Environment Isolation

**Selalu jalankan Frida dalam environment yang terisolasi:**

```bash
# Gunakan VM atau container untuk analisis
docker run --rm -v "$(pwd):/workspace" dexforge:latest frida run ...

# Atau gunakan dedicated device untuk testing
```

**Alasan:**
- Frida script memiliki akses penuh ke memory dan API aplikasi target
- Script yang tidak terpercaya bisa mencuri data sensitif
- Malware analysis harus dilakukan dalam sandbox

### 2. Script Validation

**Jangan jalankan Frida script dari sumber yang tidak terpercaya:**

```javascript
// ❌ JANGAN lakukan ini
eval(userProvidedScript);

// ✅ Lakukan validasi terlebih dahulu
if (isScriptSafe(script)) {
    execute(script);
}
```

**Checklist validasi:**
- [ ] Script tidak mengandung `eval()` atau `Function()` constructor
- [ ] Script tidak mencoba mengakses file sistem lokal
- [ ] Script tidak mencoba membuat network connection ke server eksternal
- [ ] Script hanya menggunakan Frida API yang diperlukan

### 3. Data Handling

**Perlakukan data yang diekstrak dengan hati-hati:**

```java
// ❌ JANGAN log data sensitif
console.log("Password: " + password);

// ✅ Gunakan placeholder atau hash
console.log("Password: [REDACTED]");
```

**Kategori data sensitif:**
- Credentials (password, API keys, tokens)
- Personal Identifiable Information (PII)
- Session cookies dan authentication tokens
- Cryptographic keys dan certificates
- Database credentials

### 4. APK Patching Security

**Saat mempatch APK dengan frida-gadget:**

```bash
# Verifikasi checksum APK original sebelum patch
sha256sum original.apk

# Simpan APK original di lokasi yang aman
cp original.apk /secure/location/

# Verifikasi APK yang dipatch
sha256sum patched.apk
```

**Risiko:**
- APK yang dipatch bisa berisi malware jika source tidak terpercaya
- Proses patch bisa memodifikasi kode aplikasi secara tidak terduga
- Selalu verifikasi APK di environment yang aman

### 5. Network Security

**Frida bisa digunakan untuk monitoring network traffic:**

```javascript
// Monitor network requests
Java.perform(function() {
    var OkHttpClient = Java.use('okhttp3.OkHttpClient');
    OkHttpClient.newCall.implementation = function(request) {
        console.log("[*] Request: " + request.url());
        return this.newCall(request);
    };
});
```

**Peringatan:**
- Monitoring network bisa menangkap data sensitif
- Jangan log atau simpan data yang tidak Anda miliki
- Hapus log setelah analisis selesai

### 6. Device Security

**Saat menggunakan perangkat fisik:**

```bash
# Verifikasi device sebelum analisis
adb devices
adb shell getprop ro.build.version.release
adb shell getprop ro.product.model

# Gunakan dedicated device untuk malware analysis
# Jangan gunakan device pribadi untuk analisis malware
```

**Checklist:**
- [ ] Gunakan device yang tidak berisi data sensitif
- [ ] Factory reset device sebelum dan setelah analisis malware
- [ ] Jangan install APK yang tidak terpercaya di device pribadi
- [ ] Gunakan emulator untuk analisis yang tidak memerlukan hardware-specific

### 7. Script Injection Prevention

**Hindari injection dalam Frida script:**

```javascript
// ❌ Vulnerable to injection
var className = Java.use(userInput);

// ✅ Validate input
var validClasses = Java.enumerateLoadedClassesSync();
if (validClasses.includes(userInput)) {
    var className = Java.use(userInput);
}
```

### 8. Logging Best Practices

**Jangan log informasi sensitif:**

```javascript
// ❌ Bad
console.log("Credit Card: " + creditCardNumber);

// ✅ Good
console.log("Credit Card: [REDACTED]");

// ✅ Good - log metadata only
console.log("Payment method used: " + paymentType);
```

## Reporting Security Issues

Jika Anda menemukan vulnerability dalam DexForge:

1. **Jangan buat issue public** untuk security vulnerability
2. Kirim email ke security contact (akan ditambahkan)
3. Sertakan:
   - Deskripsi vulnerability
   - Steps to reproduce
   - Potential impact
   - Suggested fix (jika ada)

## Compliance

DexForge mematuhi:

- **Apache 2.0 License** - Lihat [LICENSE](LICENSE)
- **GDPR** - Data yang diproses oleh DexForge adalah milik pengguna
- **Local Laws** - Pengguna bertanggung jawab mematuhi hukum lokal

## Checklist Sebelum Analisis

Gunakan checklist ini sebelum memulai analisis:

- [ ] Saya memiliki hak untuk menganalisis aplikasi target
- [ ] Saya menggunakan environment yang terisolasi (VM/container)
- [ ] Saya telah membackup data penting di device
- [ ] Saya memahami implikasi legal dari analisis ini
- [ ] Saya tidak akan menyebarkan data sensitif yang ditemukan
- [ ] Saya akan menghapus semua data analisis setelah selesai

## Resources

- [OWASP Mobile Security Testing Guide](https://owasp.org/www-project-mobile-security-testing-guide/)
- [Frida Security Best Practices](https://frida.re/docs/security/)
- [Android Security Best Practices](https://source.android.com/docs/security)

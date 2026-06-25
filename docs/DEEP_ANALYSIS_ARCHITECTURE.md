# Arsitektur Deep Analysis DexForge

Dokumen ini menjelaskan mekanisme analisis kode otomatis yang membedakan DexForge dari decompiler tradisional.

## Filosofi
Deep Analysis di DexForge dirancang untuk memberikan wawasan (insight) yang bermakna di atas kode mentah. Alih-alih hanya menampilkan kode, DexForge mencoba memahami *apa yang salah* atau *apa yang berbahaya* di dalam kode tersebut.

## Struktur Komponen

### 1. DexForgeAnalyzer (API Layer)
Interface dasar untuk semua detektor. Setiap analyzer harus mengimplementasikan metode `analyze(DexForgeProject)`.
- **Location**: `dexforge.api.analysis.DexForgeAnalyzer`

### 2. DexForgeFinding (API Layer)
Objek yang mewakili temuan analisis. Berisi tipe temuan, tingkat keparahan (Severity), dan lokasi (`DexForgeNode`) di mana masalah ditemukan.
- **Location**: `dexforge.api.analysis.DexForgeFinding`

### 3. AnalysisApplicationService (Core Layer)
Layanan orkestrator yang mengelola daftar analyzer yang terdaftar dan menjalankan semuanya secara berurutan.
- **Location**: `dexforge.core.analysis.AnalysisApplicationService`

---

## Contoh Penggunaan (API)

```java
DexForgeProject project = DexForgeDecompiler.open(new File("app.apk"));
List<DexForgeFinding> findings = project.runAnalysis();

for (DexForgeFinding finding : findings) {
    System.out.println("Temuan: " + finding.getMessage());
    System.out.println("Tingkat Bahaya: " + finding.getSeverity());
}
```

## Cara Menambahkan Analyzer Baru
Untuk menambahkan deteksi baru (misalnya deteksi SSL Pinning yang lemah), buatlah kelas baru yang mengimplementasikan `DexForgeAnalyzer` dan daftarkan ke `AnalysisApplicationService`.

Analisis dilakukan pada level **AST (Abstract Syntax Tree)** atau **Source Code** yang disediakan oleh `DexForgeProject`, sehingga analyzer tidak perlu tahu detail tentang JADX.

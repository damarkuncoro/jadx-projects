# DexForge Android XML Layout Viewer Roadmap

## Ringkasan

Android XML Layout Viewer adalah fitur `DexForge GUI` untuk membantu reverse engineer membaca resource UI Android hasil decompile APK tanpa membuka Android Studio.

Target utamanya bukan meniru Android Studio 100%, tetapi menyediakan preview visual ringan yang:

- Membaca XML layout Android dari `res/layout*`.
- Menampilkan struktur view sebagai tree.
- Menampilkan preview Swing sederhana.
- Menyelesaikan resource dasar seperti string, color, dimen, style, drawable, dan mipmap.
- Memperlihatkan atribut raw dan resolved agar developer paham asal tampilan.
- Tetap aman saat XML/resource tidak lengkap, malformed, atau unsupported.

## Kondisi Implementasi Saat Ini

Fitur berada di package:

```text
com.dexforge.layoutviewer
```

Integrasi GUI dilakukan dari:

```text
jadx-gui/src/main/java/jadx/gui/treemodel/JResource.java
```

Resource XML yang diarahkan ke viewer:

```text
res/layout/*.xml
res/layout-*/*.xml
res/navigation/*.xml
res/menu/*.xml
res/drawable/*.xml
res/drawable-*/*.xml
```

Komponen yang sudah ada:

```text
parser/
  SecureXml
  LayoutXmlParser
  DrawableXmlParser

model/
  AndroidViewNode
  AndroidResource
  RenderStyle

resolver/
  AndroidResourceLoader
  ResourceRefs
  ResourceResolver

renderer/
  LayoutRenderer
  RenderStyleMapper

ui/
  LayoutViewerPanel
  LayoutTreePanel
  AttributeInspectorPanel
  ResourceInspectorPanel
```

Kemampuan yang sudah ada:

- XML layout parser berbasis DOM dengan secure XML settings.
- Drawable XML parser dasar untuk `shape`, `selector`, dan `layer-list`.
- Resolver untuk `@string`, `@color`, `@dimen`, `@drawable`, `@mipmap`, `@style`.
- Style inheritance dasar.
- Nested style resolution untuk `textAppearance` dan beberapa style reference.
- Preview Swing untuk view umum seperti `LinearLayout`, `FrameLayout`, `ScrollView`, `TextView`, `Button`, `EditText`, `ImageView`, `RecyclerView`, dan `CardView`.
- Placeholder untuk `TextView` tanpa `android:text`, memakai `android:id`.
- `visibility="gone"` tidak mengambil ruang preview.
- Klik preview memilih node, menampilkan attributes, dan sinkron ke tree.
- Resource tab dengan filter cepat.

## Prinsip Arsitektur

### Separation of Concerns

Setiap layer harus punya tanggung jawab jelas:

- Parser hanya mengubah XML menjadi model.
- Loader hanya membaca resource project/decompiled APK.
- Resolver hanya menyelesaikan reference dan style ke node.
- Renderer hanya mengubah model resolved menjadi komponen Swing.
- UI hanya menyusun panel dan interaction.

### DRY

Logika bersama harus dipusatkan:

- Secure XML parsing di `SecureXml`.
- Path/reference resource di `ResourceRefs`.
- Android attribute ke Swing style di `RenderStyleMapper`.

Jangan menambahkan parsing XML, parsing dimen, atau parsing color ad hoc di panel UI.

### SOLID

Aturan pengembangan:

- `LayoutRenderer` tidak boleh terus membesar untuk semua view Android.
- Tambahkan renderer khusus per view type melalui registry.
- `ResourceResolver` tidak boleh membaca file langsung.
- `AndroidResourceLoader` tidak boleh mengatur rendering.
- Panel UI tidak boleh menyimpan business logic parsing/resolving.

### Scalable

Fitur baru harus mudah ditambahkan tanpa memodifikasi banyak class sekaligus.

Target jangka menengah:

```text
ViewRendererRegistry
  -> LinearLayoutRenderer
  -> FrameLayoutRenderer
  -> TextViewRenderer
  -> ImageViewRenderer
  -> ConstraintLayoutRenderer
  -> MaterialComponentRenderer
```

## Prioritas Roadmap

## 1. Tambahkan Test untuk AndroidResourceLoader

`AndroidResourceLoader` sekarang menjadi komponen penting karena membaca semua resource project.

Test minimum:

- `values.xml` membaca `string`, `color`, `dimen`, dan `style`.
- `values-night` dan `values-vXX` tetap terbaca sebagai kandidat resource.
- `drawable/*.xml` dibaca sebagai drawable style.
- File drawable image tetap dicatat sebagai resource path.
- XML malformed tidak menjatuhkan preview.

Acceptance criteria:

- Test dapat berjalan dengan `./gradlew :jadx-gui:test --tests 'com.dexforge.layoutviewer.*'`.
- Loader tetap best-effort saat sebagian resource gagal parse.

## 2. Buat ViewRenderer Registry

Saat ini dispatch view type masih berada di `LayoutRenderer`.

Target refactor:

```java
interface ViewRenderer {
	boolean supports(AndroidViewNode node);
	JComponent render(AndroidViewNode node, RenderContext context);
}
```

Komponen pendukung:

```text
RenderContext
  - RenderStyleMapper
  - child renderer callback
  - selection callback
```

Renderer awal:

- `LinearLayoutRenderer`
- `FrameLayoutRenderer`
- `ScrollViewRenderer`
- `TextViewRenderer`
- `BasicLeafRenderer`
- `FallbackContainerRenderer`

Acceptance criteria:

- Menambah support view baru tidak perlu mengubah switch besar di `LayoutRenderer`.
- `LayoutRenderer` menjadi orchestrator/entry point saja.

## 3. Tambahkan Diagnostic Panel

Preview reverse engineering harus jujur tentang keterbatasannya.

Diagnostic panel sebaiknya menampilkan:

- Unresolved resource reference, misalnya `@style/foo`.
- Unsupported view tag.
- Unsupported atau ignored attributes.
- Drawable XML yang gagal parse.
- Style cycle yang terdeteksi.

Contoh output:

```text
Warnings
- Unresolved dimen: @dimen/abc_action_bar_subtitle_top_margin_material
- Unsupported view: com.google.android.material.textfield.TextInputLayout
- Ignored attr: app:layout_constraintTop_toTopOf
```

Acceptance criteria:

- User bisa membedakan preview akurat, best-effort, dan unsupported.
- Warning tidak menghentikan rendering.

## 4. Handle AppCompat, AndroidX, dan Material Aliases

Banyak APK modern memakai fully-qualified view tag.

Mapping awal:

```text
androidx.appcompat.widget.Toolbar -> LinearLayout-like toolbar preview
androidx.appcompat.widget.AppCompatTextView -> TextView
androidx.appcompat.widget.AppCompatButton -> Button
androidx.constraintlayout.widget.ConstraintLayout -> ConstraintLayout
com.google.android.material.textfield.TextInputLayout -> container
com.google.android.material.button.MaterialButton -> Button
```

Acceptance criteria:

- Fully-qualified tags umum tidak jatuh ke fallback generik jika ada equivalent renderer.
- Attribute inspector tetap menampilkan tag asli.

## 5. Perbaiki Layout Semantics

Setelah registry siap, polish visual dapat dilakukan bertahap.

Prioritas:

- `android:gravity`
- `android:layout_gravity`
- `android:layout_weight`
- `android:minWidth`, `android:minHeight`
- `android:maxLines`
- `android:ellipsize`
- `android:includeFontPadding`
- `android:tint`
- `android:scaleType`

Acceptance criteria:

- Layout AppCompat action bar, toolbar, login form, dan card list menjadi lebih mudah dikenali.
- Perubahan tetap tidak membuat renderer brittle.

## 6. ConstraintLayout dan RelativeLayout Best-Effort

Jangan langsung mencoba implementasi penuh.

Tahap awal:

- Render children secara vertical jika constraints tidak lengkap.
- Gunakan `layout_constraintTop_toTopOf`, `layout_constraintStart_toStartOf`, dll sebagai hint ordering.
- Tampilkan warning untuk constraint yang diabaikan.

Acceptance criteria:

- Preview tetap berguna walau tidak pixel-perfect.
- Diagnostic panel menjelaskan constraint yang belum didukung.

## Non-Goals

Untuk menjaga scope tetap sehat:

- Tidak mengejar rendering pixel-perfect Android Studio.
- Tidak menjalankan Android runtime.
- Tidak mengevaluasi theme runtime secara penuh.
- Tidak resolve `?attr` secara lengkap sebelum resource/theme pipeline lebih matang.
- Tidak menambahkan dependency berat hanya untuk preview MVP.

## Suggested Next Milestone

Milestone terbaik berikutnya:

```text
Diagnostic Panel + ViewRenderer Registry
```

Alasannya:

- Diagnostic panel membuat preview lebih dapat dipercaya.
- Renderer registry membuat fitur baru lebih scalable.
- Keduanya memperkuat Clean Architecture tanpa menambah kompleksitas visual terlalu cepat.

## Command Verifikasi

Gunakan command berikut setelah perubahan:

```bash
./gradlew :jadx-gui:compileJava :jadx-gui:test --tests 'com.dexforge.layoutviewer.*'
```

Untuk mencoba GUI:

```bash
./gradlew :jadx-gui:run
```

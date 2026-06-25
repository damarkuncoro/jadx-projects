**Smali parser** adalah bagian program yang tugasnya **membaca file `.smali`**, memahami strukturnya, lalu mengubahnya menjadi bentuk data yang bisa diproses oleh aplikasi.

Sederhananya:

```text
file .smali
   ↓
Smali Parser
   ↓
struktur data / AST / model class
   ↓
bisa dianalisis, diedit, divalidasi, atau dikompilasi lagi
```

Contoh file smali:

```smali
.class public Lcom/example/MainActivity;
.super Landroid/app/Activity;

.method public onCreate(Landroid/os/Bundle;)V
    .registers 2

    invoke-super {p0, p1}, Landroid/app/Activity;->onCreate(Landroid/os/Bundle;)V

    return-void
.end method
```

Smali parser akan membaca bagian-bagian seperti:

```text
.class  → nama class
.super  → parent class
.method → method
.registers → jumlah register
invoke-super → instruksi bytecode
return-void → instruksi return
.end method → akhir method
```

Hasil parsing-nya bisa menjadi model seperti ini:

```ts
{
  className: "Lcom/example/MainActivity;",
  access: ["public"],
  superClass: "Landroid/app/Activity;",
  methods: [
    {
      name: "onCreate",
      returnType: "V",
      parameters: ["Landroid/os/Bundle;"],
      registers: 2,
      instructions: [
        {
          opcode: "invoke-super",
          args: ["p0", "p1"],
          target: "Landroid/app/Activity;->onCreate(Landroid/os/Bundle;)V"
        },
        {
          opcode: "return-void"
        }
      ]
    }
  ]
}
```

## Fungsi utama Smali Parser

Dalam aplikasi seperti **DexForge**, smali parser berguna untuk:

```text
1. Membaca file .smali
2. Membuat class tree
3. Menampilkan daftar method dan field
4. Mengecek error syntax
5. Memberi syntax highlighting
6. Membuat fitur search symbol
7. Membantu autocomplete opcode
8. Mengubah smali menjadi struktur yang bisa diedit
9. Menyiapkan proses assemble balik ke DEX
```

## Bedanya parser dan assembler

Ini penting:

```text
Smali Parser
= membaca dan memahami file .smali

Smali Assembler
= mengubah file .smali menjadi classes.dex

Smali Disassembler
= mengubah classes.dex menjadi file .smali
```

Alurnya:

```text
classes.dex
   ↓ disassembler
.smali
   ↓ parser
model data / AST
   ↓ editor
.smali
   ↓ assembler
classes.dex
```

## Contoh algoritma Smali Parser

Secara sederhana:

```text
1. Baca file baris per baris
2. Abaikan komentar dan baris kosong
3. Deteksi directive seperti .class, .super, .field, .method
4. Saat masuk .method, kumpulkan instruksi sampai .end method
5. Parse opcode dan operand
6. Validasi label, register, tipe data, dan signature method
7. Simpan hasilnya ke AST/model
```

Contoh pseudo-code:

```ts
for (const line of smaliLines) {
  if (line.startsWith(".class")) {
    parseClass(line)
  } else if (line.startsWith(".super")) {
    parseSuper(line)
  } else if (line.startsWith(".field")) {
    parseField(line)
  } else if (line.startsWith(".method")) {
    currentMethod = parseMethod(line)
  } else if (line.startsWith(".end method")) {
    saveMethod(currentMethod)
    currentMethod = null
  } else if (currentMethod) {
    parseInstruction(line)
  }
}
```

## Contoh bagian yang diparse

### 1. Class

```smali
.class public Lcom/example/User;
```

Menjadi:

```json
{
  "type": "class",
  "access": ["public"],
  "name": "Lcom/example/User;"
}
```

### 2. Field

```smali
.field private name:Ljava/lang/String;
```

Menjadi:

```json
{
  "type": "field",
  "access": ["private"],
  "name": "name",
  "fieldType": "Ljava/lang/String;"
}
```

### 3. Method

```smali
.method public getName()Ljava/lang/String;
```

Menjadi:

```json
{
  "type": "method",
  "access": ["public"],
  "name": "getName",
  "params": [],
  "returnType": "Ljava/lang/String;"
}
```

### 4. Instruction

```smali
invoke-virtual {p0}, Lcom/example/User;->getName()Ljava/lang/String;
```

Menjadi:

```json
{
  "opcode": "invoke-virtual",
  "registers": ["p0"],
  "targetClass": "Lcom/example/User;",
  "targetMethod": "getName",
  "returnType": "Ljava/lang/String;"
}
```

## Untuk DexForge, arsitektur parser-nya bisa begini

```text
SmaliTokenizer
   ↓
SmaliParser
   ↓
AST Builder
   ↓
Semantic Analyzer
   ↓
Editor Services
```

Penjelasan:

```text
SmaliTokenizer
= memecah teks menjadi token

SmaliParser
= memahami susunan token

AST Builder
= membuat struktur data class/method/field

Semantic Analyzer
= validasi register, label, tipe, opcode

Editor Services
= autocomplete, outline, go to definition, error marker
```

Contoh struktur folder:

```text
dexforge-smali/
├── tokenizer/
│   └── SmaliTokenizer.ts
├── parser/
│   └── SmaliParser.ts
├── ast/
│   ├── SmaliClass.ts
│   ├── SmaliMethod.ts
│   ├── SmaliField.ts
│   └── SmaliInstruction.ts
├── analyzer/
│   └── SmaliSemanticAnalyzer.ts
└── editor/
    ├── CompletionProvider.ts
    ├── DiagnosticsProvider.ts
    └── SymbolProvider.ts
```

Jadi, **Smali Parser adalah mesin pembaca kode smali**. Tanpa parser, aplikasi hanya melihat `.smali` sebagai teks biasa. Dengan parser, aplikasi bisa memahami bahwa teks itu adalah class, method, field, register, label, dan instruksi Android bytecode.

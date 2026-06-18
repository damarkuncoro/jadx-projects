# DexForge Engine

<img src="jadx-gui/src/main/resources/logos/dexforge-logo.svg" width="96" align="right" alt="DexForge logo" />

**DexForge Engine** is an Android reverse engineering workbench powered by the upstream [`skylot/jadx`](https://github.com/skylot/jadx) decompiler.

DexForge keeps JADX compatibility where it matters, then adds a modern workflow layer for APK analysis, device extraction, Frida scripting, binary/resource inspection, IDE automation, and Android layout preview.

![GitHub release](https://img.shields.io/github/v/release/damarkuncoro/jadx-projects?label=release&logo=github)
![GitHub downloads](https://img.shields.io/github/downloads/damarkuncoro/jadx-projects/total)
![GitHub contributors](https://img.shields.io/github/contributors/damarkuncoro/jadx-projects)
![Java 11+](https://img.shields.io/badge/runtime-Java%2011%2B-blue)
![Build JDK 17+](https://img.shields.io/badge/build-JDK%2017%2B-orange)
[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

## Why DexForge

DexForge is designed for practical Android reverse engineering:

- decompile APK, DEX, AAB, AAR, JAR, ZIP, class, smali, XAPK, APKM, and APKS inputs
- inspect Java output, smali, resources, manifests, native libraries, and binary XML
- pull base and split APKs directly from a connected Android device
- generate Frida hooks from GUI context
- preview Android XML layouts without opening Android Studio
- expose automation-friendly CLI and JSON/LSP workflows for IDE integrations

> [!IMPORTANT]
> DexForge is a fork and extension layer built on JADX. The `dexforge` and `dexforge-gui` commands are the preferred DexForge entry points; `jadx` and `jadx-gui` remain available as compatibility aliases.

> [!WARNING]
> Decompilation is best-effort. DexForge/JADX may produce incomplete or incorrect code for heavily obfuscated, optimized, or malformed inputs. Always validate important findings against bytecode, resources, and runtime behavior.

## Feature Highlights

### DexForge GUI

- syntax-highlighted decompiled Java and resource viewing
- declaration navigation, usage lookup, and full-text search
- smali debugger support inherited from JADX
- native library inspection with ELF header parsing and hex viewing
- automatic binary Android XML detection and decoding
- Android XML Layout Viewer with preview, tree, resolved attributes, resources, and XML source tabs
- Frida panel and method-context hook generation
- Device Explorer under `File -> Open from Android Device...`

### DexForge Device Explorer

- detect connected Android devices through ADB
- list packages by Android user/profile
- resolve `base.apk` and split APK paths
- pull APK sets into a structured workspace
- open or decompile pulled packages
- emit machine-readable JSON for IDE integrations

### DexForge Layout Viewer

- opens Android layout/resource XML inside DexForge GUI
- resolves common `@string`, `@color`, `@dimen`, `@style`, `@drawable`, and `@mipmap` references
- supports basic visual previews for common Android views
- shows raw and resolved attributes side by side
- syncs preview clicks with the layout tree and inspector

### Frida Integration

- generate Frida hook scripts from selected methods
- edit and run scripts from the GUI panel
- use predefined snippets for common Android runtime analysis workflows

### Automation and IDE Readiness

- `device-explorer` CLI command with optional JSON output
- `lsp` / `decompiler-daemon` JSON-RPC mode for editor integrations
- repository split plan for VS Code, IntelliJ, and public docs

## Download

Download packaged releases from:

[GitHub Releases](https://github.com/damarkuncoro/jadx-projects/releases/latest)

Build artifacts produced by this repository use DexForge names:

```text
build/dexforge-engine-<version>.zip
build/dexforge-gui-<version>-win.zip
build/dexforge-gui-<version>-with-jre-win.zip
```

After unpacking the engine zip, run from `bin`:

| Command | Purpose |
| --- | --- |
| `dexforge` | primary DexForge CLI |
| `dexforge-gui` | primary DexForge desktop GUI |
| `jadx` | compatibility CLI alias |
| `jadx-gui` | compatibility GUI alias |

On Windows, use the corresponding `.bat` files.

Runtime requirement:

```text
Java 11 or later
```

## Build From Source

Build requirement:

```text
JDK 17 or later
```

Build the distribution:

```bash
git clone https://github.com/damarkuncoro/jadx-projects.git
cd jadx-projects
./gradlew dist
```

Run the GUI during development:

```bash
./gradlew :jadx-gui:run
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

## Release Helper

This repository includes a helper for publishing generated release assets.

Build first:

```bash
./gradlew dist
```

Create a release:

```bash
./scripts/release.sh v1.5.6
```

Attach explicit release notes:

```bash
./scripts/release.sh v1.5.6 --artifact build/dexforge-engine-dev.zip --notes-file release-notes.md
```

Latest release notes are stored in [`release-notes.md`](release-notes.md).

## Documentation

- [Project requirements](docs/PROJECT_REQUIREMENTS.md)
- [DexForge repository split](docs/DEXFORGE_REPOSITORIES.md)
- [DexForge rebranding roadmap](docs/DEXFORGE_REBRANDING_ROADMAP.md)
- [Android XML Layout Viewer roadmap](docs/ANDROID_XML_LAYOUT_VIEWER_ROADMAP.md)
- [Device Explorer roadmap](docs/DEVICE_EXPLORER_ROADMAP.md)

## Upstream Compatibility

DexForge intentionally preserves JADX compatibility:

- internal Gradle modules such as `jadx-core`, `jadx-cli`, and `jadx-gui` remain unchanged
- Java packages under `jadx.*` remain available for upstream compatibility
- `jadx` and `jadx-gui` commands are still shipped as aliases
- upstream JADX docs and troubleshooting remain useful for core decompiler behavior

For library usage of upstream JADX APIs, see the upstream guide:

[Use jadx as a library](https://github.com/skylot/jadx/wiki/Use-jadx-as-a-library)

### Usage
```
dexforge[-gui] [command] [options] <input files> (.apk, .dex, .jar, .class, .smali, .zip, .aar, .arsc, .aab, .xapk, .apkm, .jadx.kts)
commands (use '<command> --help' for command options):
  plugins         - manage jadx plugins
  device-explorer - pull and decompile APKs from Android devices via ADB
  lsp             - launch the high-performance Language Server Protocol JSON-RPC daemon (alias: decompiler-daemon)

options:
  -d, --output-dir                              - output directory
  -ds, --output-dir-src                         - output directory for sources
  -dr, --output-dir-res                         - output directory for resources
  -r, --no-res                                  - do not decode resources
  -s, --no-src                                  - do not decompile source code
  -j, --threads-count                           - processing threads count, default: 16
  --single-class                                - decompile a single class, full name, raw or alias
  --single-class-output                         - file or dir for write if decompile a single class
  --output-format                               - can be 'java' or 'json', default: java
  -e, --export-gradle                           - save as gradle project (set '--export-gradle-type' to 'auto')
  --export-gradle-type                          - Gradle project template for export:
                                                   'auto' - detect automatically
                                                   'android-app' - Android Application (apk)
                                                   'android-library' - Android Library (aar)
                                                   'simple-java' - simple Java
  -m, --decompilation-mode                      - code output mode:
                                                   'auto' - trying best options (default)
                                                   'restructure' - restore code structure (normal java code)
                                                   'simple' - simplified instructions (linear, with goto's)
                                                   'fallback' - raw instructions without modifications
  --show-bad-code                               - show inconsistent code (incorrectly decompiled)
  --no-xml-pretty-print                         - do not prettify XML
  --no-imports                                  - disable use of imports, always write entire package name
  --no-debug-info                               - disable debug info parsing and processing
  --add-debug-lines                             - add comments with debug line numbers if available
  --no-inline-anonymous                         - disable anonymous classes inline
  --no-inline-methods                           - disable methods inline
  --no-move-inner-classes                       - disable move inner classes into parent
  --no-inline-kotlin-lambda                     - disable inline for Kotlin lambdas
  --no-finally                                  - don't extract finally block
  --no-restore-switch-over-string               - don't restore switch over string
  --no-replace-consts                           - don't replace constant value with matching constant field
  --escape-unicode                              - escape non latin characters in strings (with \u)
  --exclude-zz                                  - exclude/ignore classes starting with 'zz' from decompilation and output
  --respect-bytecode-access-modifiers           - don't change original access modifiers
  --mappings-path                               - deobfuscation mappings file or directory. Allowed formats: Tiny and Tiny v2 (both '.tiny'), Enigma (.mapping) or Enigma directory
  --mappings-mode                               - set mode for handling the deobfuscation mapping file:
                                                   'read' - just read, user can always save manually (default)
                                                   'read-and-autosave-every-change' - read and autosave after every change
                                                   'read-and-autosave-before-closing' - read and autosave before exiting the app or closing the project
                                                   'ignore' - don't read or save (can be used to skip loading mapping files referenced in the project file)
  --deobf                                       - activate deobfuscation
  --deobf-min                                   - min length of name, renamed if shorter, default: 3
  --deobf-max                                   - max length of name, renamed if longer, default: 64
  --deobf-whitelist                             - space separated list of classes (full name) and packages (ends with '.*') to exclude from deobfuscation, default: android.support.v4.* android.support.v7.* android.support.v4.os.* android.support.annotation.Px androidx.core.os.* androidx.annotation.Px
  --deobf-cfg-file                              - deobfuscation mappings file used for JADX auto-generated names (in the JOBF file format), default: same dir and name as input file with '.jobf' extension
  --deobf-cfg-file-mode                         - set mode for handling the JADX auto-generated names' deobfuscation map file:
                                                   'read' - read if found, don't save (default)
                                                   'read-or-save' - read if found, save otherwise (don't overwrite)
                                                   'overwrite' - don't read, always save
                                                   'ignore' - don't read and don't save
  --deobf-res-name-source                       - better name source for resources:
                                                   'auto' - automatically select best name (default)
                                                   'resources' - use resources names
                                                   'code' - use R class fields names
  --use-source-name-as-class-name-alias         - use source name as class name alias:
                                                   'always' - always use source name if it's available
                                                   'if-better' - use source name if it seems better than the current one
                                                   'never' - never use source name, even if it's available
  --source-name-repeat-limit                    - allow using source name if it appears less than a limit number, default: 10
  --use-kotlin-methods-for-var-names            - use kotlin intrinsic methods to rename variables, values: disable, apply, apply-and-hide, default: apply
  --use-headers-for-detect-resource-extensions  - Use headers for detect resource extensions if resource obfuscated
  --rename-flags                                - fix options (comma-separated list of):
                                                   'case' - fix case sensitivity issues (according to --fs-case-sensitive option),
                                                   'valid' - rename java identifiers to make them valid,
                                                   'printable' - remove non-printable chars from identifiers,
                                                  or single 'none' - to disable all renames
                                                  or single 'all' - to enable all (default)
  --integer-format                              - how integers are displayed:
                                                   'auto' - automatically select (default)
                                                   'decimal' - use decimal
                                                   'hexadecimal' - use hexadecimal
  --type-update-limit                           - type update limit count (per one instruction), default: 10
  --fs-case-sensitive                           - treat filesystem as case sensitive, false by default
  --cfg                                         - save methods control flow graph to dot file
  --raw-cfg                                     - save methods control flow graph (use raw instructions)
  -f, --fallback                                - set '--decompilation-mode' to 'fallback' (deprecated)
  --use-dx                                      - use dx/d8 to convert java bytecode
  --comments-level                              - set code comments level, values: error, warn, info, debug, user-only, none, default: info
  --log-level                                   - set log level, values: quiet, progress, error, warn, info, debug, default: progress
  -v, --verbose                                 - verbose output (set --log-level to DEBUG)
  -q, --quiet                                   - turn off output (set --log-level to QUIET)
  --disable-plugins                             - comma separated list of plugin ids to disable
  --config <config-ref>                         - load configuration from file, <config-ref> can be:
                                                   path to '.json' file
                                                   short name - uses file with this name from config directory
                                                   'none' - to disable config loading
  --save-config <config-ref>                    - save current options into configuration file and exit, <config-ref> can be:
                                                   empty - for default config
                                                   path to '.json' file
                                                   short name - file will be saved in config directory
  --print-files                                 - print files and directories used by jadx (config, cache, temp)
  --version                                     - print jadx version
  -h, --help                                    - print this help

Plugin options (-P<name>=<value>):
  dex-input: Load .dex and .apk files
    - dex-input.verify-checksum                 - verify dex file checksum before load, values: [yes, no], default: yes
  java-convert: Convert .class, .jar and .aar files to dex
    - java-convert.mode                         - convert mode, values: [dx, d8, both], default: both
    - java-convert.d8-desugar                   - use desugar in d8, values: [yes, no], default: no
  kotlin-metadata: Use kotlin.Metadata annotation for code generation
    - kotlin-metadata.class-alias               - rename class alias, values: [yes, no], default: yes
    - kotlin-metadata.method-args               - rename function arguments, values: [yes, no], default: yes
    - kotlin-metadata.fields                    - rename fields, values: [yes, no], default: yes
    - kotlin-metadata.companion                 - rename companion object, values: [yes, no], default: yes
    - kotlin-metadata.data-class                - add data class modifier, values: [yes, no], default: yes
    - kotlin-metadata.to-string                 - rename fields using toString, values: [yes, no], default: yes
    - kotlin-metadata.getters                   - rename simple getters to field names, values: [yes, no], default: yes
  kotlin-smap: Use kotlin.SourceDebugExtension annotation for rename class alias
    - kotlin-smap.class-alias-source-dbg        - rename class alias from SourceDebugExtension, values: [yes, no], default: no
  rename-mappings: various mappings support
    - rename-mappings.format                    - mapping format, values: [AUTO, TINY_FILE, TINY_2_FILE, ENIGMA_FILE, ENIGMA_DIR, PROGUARD_FILE, SRG_FILE, XSRG_FILE, JAM_FILE, CSRG_FILE, TSRG_FILE, TSRG_2_FILE, INTELLIJ_MIGRATION_MAP_FILE, RECAF_SIMPLE_FILE, JOBF_FILE], default: AUTO
    - rename-mappings.invert                    - invert mapping on load, values: [yes, no], default: no
  smali-input: Load .smali files
    - smali-input.api-level                     - Android API level, default: 27

Environment variables:
  JADX_DISABLE_XML_SECURITY - set to 'true' to disable all security checks for XML files
  JADX_DISABLE_ZIP_SECURITY - set to 'true' to disable all security checks for zip files
  JADX_ZIP_MAX_ENTRIES_COUNT - maximum allowed number of entries in zip files (default: 100 000)
  JADX_CONFIG_DIR - custom config directory, using system by default
  JADX_CACHE_DIR - custom cache directory, using system by default
  JADX_TMP_DIR - custom temp directory, using system by default

Examples:
  jadx -d out classes.dex
  jadx --rename-flags "none" classes.dex
  jadx --rename-flags "valid, printable" classes.dex
  jadx --log-level ERROR app.apk
  jadx -Pdex-input.verify-checksum=no app.apk
```
These options also work in DexForge GUI running from command line and override options from preferences' dialog

Usage for `plugins` command
```
usage: plugins [options]
options:
  -i, --install <locationId>      - install plugin with locationId
  -j, --install-jar <path-to.jar> - install plugin from jar file
  -l, --list                      - list installed plugins
  -a, --available                 - list available plugins from jadx-plugins-list (aka marketplace)
  -u, --update                    - update installed plugins
  --uninstall <pluginId>          - uninstall plugin with pluginId
  --disable <pluginId>            - disable plugin with pluginId
  --enable <pluginId>             - enable plugin with pluginId
  --list-all                      - list all plugins including bundled and dropins
  --list-versions <locationId>    - fetch latest versions of plugin from locationId (will download all artefacts, limited to 10)
  -h, --help                      - print this help
```

Usage for `device-explorer` command
```
usage: dexforge device-explorer <command> [options] [--format json]
commands:
  list-devices                                                  - list connected devices via ADB
  list-users <serial>                                           - list Android users/profiles on device
  list-packages <serial> <user_id> [filter]                     - list packages for user_id (filters: all, user, system)
  paths <serial> <package_name>                                 - resolve APK paths for package name
  pull <serial> <package_name> <out_dir> [user_id]              - pull APK and split APK components into workspace
  pull-and-decompile <serial> <package_name> <out_dir> [user_id] - pull APK splits, decompile them, and generate security reports
```

Add `--format json` to any `device-explorer` command to get machine-readable output for IDE integrations.

Examples for IDE integrations:
```bash
dexforge device-explorer list-devices --format json
dexforge device-explorer list-packages SERIAL 0 user --format json
dexforge device-explorer pull-and-decompile SERIAL id.net.cakramedia.attendance ./workspace 0 --format json
```


### LSP JSON-RPC Daemon (`lsp` / `decompiler-daemon`)
Exposes JADX decompiler capabilities as an LSP-compliant JSON-RPC service over stdin/stdout, allowing standard editor integrations (VS Code & IntelliJ) to load inputs, decompile classes, navigate symbols, and hover signatures with sub-millisecond response times.

**Commands Supported:**
* `initialize` - Registers server capabilities (`hoverProvider`, `definitionProvider`, `referencesProvider`, `workspaceSymbolProvider`).
* `load` - Loads the path of an APK/JAR.
* `list-classes` - Lists all loaded class metadata.
* `decompile` - Decompiles a class, returning the code, line mappings, and diagnostics (warnings/errors).
* `textDocument/definition` - Resolves the definition location of a symbol.
* `textDocument/references` - Resolves all usage reference locations of a symbol.
* `textDocument/hover` - Exposes the formatted Java signature of a symbol.
* `workspace/symbol` - Fuzzy searches all classes, methods, and fields globally.
* `exit` - Shuts down the daemon.

**Example execution:**
```bash
dexforge lsp
```
Send standard JSON lines on stdin (e.g. `{"id": 1, "method": "initialize"}`) to receive JSON-RPC responses on stdout.

### Troubleshooting
Please check wiki page [Troubleshooting Q&A](https://github.com/skylot/jadx/wiki/Troubleshooting-Q&A)

### Contributing
To support this project you can:
  - Post thoughts about new features/optimizations that important to you
  - Submit decompilation issues, please read before proceed: [Open issue](CONTRIBUTING.md#Open-Issue)
  - Open pull request, please follow these rules: [Pull Request Process](CONTRIBUTING.md#Pull-Request-Process)

---------------------------------------
*Licensed under the Apache 2.0 License*

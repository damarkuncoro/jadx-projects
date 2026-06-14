<img src="https://raw.githubusercontent.com/skylot/jadx/master/jadx-gui/src/main/resources/logos/jadx-logo.png" width="64" align="left" />

> **Note:** this repository is a local fork of `skylot/jadx` maintained by `damarkuncoro`.
> This fork includes custom build and release tooling, and is intended for local development and binary-only release packaging.
>
> To run the GUI from source, use `./gradlew :jadx-gui:run`.
> To create a GitHub release asset, use `./gradlew dist` and `./scripts/release.sh <tag>`.

## JADX

![GitHub release](https://img.shields.io/github/v/release/damarkuncoro/jadx-projects?label=release&logo=github)
![GitHub downloads](https://img.shields.io/github/downloads/damarkuncoro/jadx-projects/total)
![GitHub contributors](https://img.shields.io/github/contributors/damarkuncoro/jadx-projects)
![Java 11+](https://img.shields.io/badge/Java-11%2B-blue)
[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

**jadx** - Dex to Java decompiler

Command line and GUI tools for producing Java source code from Android Dex and Apk files

> [!WARNING]
> Please note that jadx may not decompile 100% of code in all cases, so errors can occur.
> For troubleshooting, upstream `skylot/jadx` wiki resources often contain useful guidance.

**Main features:**
- decompile Dalvik bytecode to Java code from APK, dex, aar, aab and zip files
- decode `AndroidManifest.xml` and other resources from `resources.arsc`
- deobfuscator included
- **JADX Device Explorer**: pull APKs (including split APK packages) directly from Android devices via ADB, with automatic workspace creation, decompile runners, and security analysis assistant scanning
- **ELF Header Parser & Hex Viewer**: view ELF file headers (Class, OS/ABI, Machine, Entry point, etc.) and raw hexadecimal contents of native libraries (`.so` files)
- **Automatic Binary XML Decoder**: signature-based detection and decoding of binary Android XML files (layout/drawable) even without standard extensions

**jadx-gui features:**
- view decompiled code with highlighted syntax
- jump to declaration
- find usage
- full text search
- smali debugger, check the upstream wiki for setup and usage
- JADX Device Explorer UI under menu `File` -> `Open from Android Device...` to visually browse packages, select split APK configuration, pull, and decompile

Jadx-gui key bindings can be found in the upstream wiki.

See these features in action in the upstream jadx documentation.

<img src="https://user-images.githubusercontent.com/118523/142730720-839f017e-38db-423e-b53f-39f5f0a0316f.png" width="700"/>

### Download
- release
  from [GitHub release](https://github.com/damarkuncoro/jadx-projects/releases/latest)
- built artifact available from `./gradlew dist` as `build/jadx-dev.zip`

After unpacking the zip file, run from `bin`:
- `jadx` - command line version
- `jadx-gui` - UI version

On Windows, run the corresponding `.bat` files.
**Note:** ensure you have Java 11 or later installed.

### Install
- Arch Linux / AUR / macOS / Flathub instructions below refer to the upstream `skylot/jadx` distribution and may not reflect this fork.
- For this fork, preferred method is to build from source and use the generated binary release asset.

- Arch Linux
  [![Arch Linux package](https://img.shields.io/archlinux/v/extra/any/jadx)](https://archlinux.org/packages/extra/any/jadx/)
  [![AUR Version](https://img.shields.io/aur/version/jadx-git)](https://aur.archlinux.org/packages/jadx-git)
  ```bash
  sudo pacman -S jadx
  ```
- macOS
  [![homebrew version](https://img.shields.io/homebrew/v/jadx)](https://formulae.brew.sh/formula/jadx)
  ```bash
  brew install jadx
  ```
- Flathub
  [![Flathub Version](https://img.shields.io/flathub/v/com.github.skylot.jadx)](https://flathub.org/apps/com.github.skylot.jadx)
  ```bash
  flatpak install flathub com.github.skylot.jadx
  ```

### Use jadx as a library
You can use jadx in your java projects, check details on [wiki page](https://github.com/skylot/jadx/wiki/Use-jadx-as-a-library)

### Build from source
JDK 17 or higher must be installed:
```
git clone https://github.com/damarkuncoro/jadx-projects.git
cd jadx-projects
./gradlew dist
```

(on Windows, use `gradlew.bat` instead of `./gradlew`)

If you only need to run the GUI during development:
```bash
./gradlew :jadx-gui:run
```

### Project requirements
For the local fork, see [`docs/PROJECT_REQUIREMENTS.md`](docs/PROJECT_REQUIREMENTS.md) for environment setup, build prerequisites, and runtime dependencies.

Scripts for run jadx will be placed in `build/jadx/bin`
and also packed to `build/jadx-<version>.zip`

### Binary release helper
This repository includes a helper script to create GitHub Releases from the generated distribution asset.

Build the distribution first:

```bash
./gradlew dist
```

Then create a release (example):

```bash
./scripts/release.sh v1.5.6
```

The helper will use `build/jadx-dev.zip` by default when it exists.

To attach custom release notes:

```bash
./scripts/release.sh v1.5.6 --artifact build/jadx-dev.zip --notes-file release-notes.md
```

Latest release notes are stored in `release-notes.md`.

### Usage
```
jadx[-gui] [command] [options] <input files> (.apk, .dex, .jar, .class, .smali, .zip, .aar, .arsc, .aab, .xapk, .apkm, .jadx.kts)
commands (use '<command> --help' for command options):
  plugins         - manage jadx plugins
  device-explorer - pull and decompile APKs from Android devices via ADB

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
These options also work in jadx-gui running from command line and override options from preferences' dialog

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
usage: device-explorer <command> [options]
commands:
  list-devices                                                  - list connected devices via ADB
  list-users <serial>                                           - list Android users/profiles on device
  list-packages <serial> <user_id> [filter]                     - list packages for user_id (filters: all, user, system)
  paths <serial> <package_name>                                 - resolve APK paths for package name
  pull <serial> <package_name> <out_dir> [user_id]              - pull APK and split APK components into workspace
  pull-and-decompile <serial> <package_name> <out_dir> [user_id] - pull APK splits, decompile them, and generate security reports
```


### Troubleshooting
Please check wiki page [Troubleshooting Q&A](https://github.com/skylot/jadx/wiki/Troubleshooting-Q&A)

### Contributing
To support this project you can:
  - Post thoughts about new features/optimizations that important to you
  - Submit decompilation issues, please read before proceed: [Open issue](CONTRIBUTING.md#Open-Issue)
  - Open pull request, please follow these rules: [Pull Request Process](CONTRIBUTING.md#Pull-Request-Process)

---------------------------------------
*Licensed under the Apache 2.0 License*

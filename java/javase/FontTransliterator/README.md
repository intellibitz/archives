# FontTransliterator

Universal desktop transliterator and phonetic font mapper for Indian and global languages, built on Java SE and modern Gradle.

---

## Overview

**FontTransliterator** (originally developed as part of the STED project) provides desktop phonetic keyboard mapping, Unicode conversion, and font transliteration capabilities across Indic scripts (including Tamil, Devanagari, and Telugu) and Latin alphabets.

## Features

- **Phonetic Keyboard Mapping**: Dynamic character keypad mapping and phonetic rule evaluation.
- **Font Conversion & Mapping**: Convert between legacy 8-bit non-Unicode font encodings (BAMINI, TAB, TAM) and modern Unicode standard.
- **Desktop GUI**: Cross-platform Java Swing interface (`STEDGUI`) with multi-document editor (`DesktopFrame`), font keypad inspector, and mapping table editor.
- **Modern Build System**: Configured with modern Gradle Kotlin DSL ([`build.gradle.kts`](./os/lang/javase/FontTransliterator/build.gradle.kts)).

## Project Structure

```
FontTransliterator/
├── build.gradle.kts        # Modern Gradle build configuration
├── src/                    # Java SE application source code
│   └── intellibitz/sted/   # Core packages: actions, launch, ui, util
├── fonts/                  # TrueType and font resource bundles
├── settings/               # Mapping XML rules (BAMINI, TAB, TAM)
└── bin/                    # Launcher scripts (sted.sh, sted.bat)
```

## Building & Running

```bash
# Build with Gradle
gradle build

# Run application
gradle run
# Or via launcher script
./bin/sted.sh
```

## License

This project is licensed under the [MIT License](./LICENSE).

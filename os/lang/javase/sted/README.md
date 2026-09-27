# STED (Simple Text Editor & Desktop)

A lightweight cross-platform text editor and desktop shell built on Java SE.

---

## Overview

**STED** provides an extensible Java Swing-based desktop editor supporting multiple document interfaces (MDI), customizable keybindings, font management, and external tooling integrations.

## Features

- **Multi-Document Interface**: Tabbed and desktop frame modes for handling multiple text buffers.
- **Customizable Environment**: Configurable look-and-feel themes, key mappings, and editor fonts.
- **Cross-Platform**: Runs across Linux/X11 and Windows.

## Project Structure

```
sted/
├── bin/          # Startup scripts (sted.sh, sted.bat)
├── config/       # Application configuration templates
├── settings/     # Editor preferences and key mappings
└── lib/          # Runtime dependencies
```

## Running STED

```bash
# On Linux / macOS:
./bin/sted.sh

# On Windows:
bin\sted.bat
```

## License

This project is licensed under the [MIT License](./LICENSE).

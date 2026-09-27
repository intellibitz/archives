# Mobeegal (`mobeegal`)

**Mobeegal** ("Find stuff closer") is a location-based mobile discovery, local search, and proximity matchmaking service.

The project pairs an Android client application utilizing XMPP messaging with a web portal and rich system design documentation.

---

## Directory Overview

```
ui/android/mobeegal/
├── client/
│   ├── android/           # Android mobile application
│   │   ├── AndroidManifest.xml
│   │   ├── build.xml
│   │   ├── res/           # UI layouts and resources
│   │   └── src/           # XMPP chat, search activities, and location handlers
│   └── docs/              # Comprehensive design documents & specifications
│       ├── mobeegal-DesignDocument.odt
│       ├── mobeegal-FunctionalSpecs.pdf
│       ├── mobeegal-TestCase.odt
│       └── mobeegal-UserGuide.odt
└── mobeegal.in/           # Web portal frontend (PHP, HTML5, CSS)
    ├── index.php
    ├── content/           # Marketing and announcement views
    ├── css/style.css
    └── include/           # Header, footer, and navigation partials
```

---

## Technical Highlights

- **Proximity Discovery**: Matches users with nearby points of interest, events, and peers using GPS and cellular triangulation coordinates.
- **XMPP Instant Messaging**: Integrates the Smack XMPP protocol library to power real-time peer-to-peer chats directly after mutual discovery matches.
- **Testing via Mock Location Provider**: Supports development and testing through mock GPS KML traces pushed to the emulator filesystem (`/data/misc/location/mobeegal`).
- **Web Portal (`mobeegal.in`)**: Serves as the landing page and registration gateway.

---

## Licensing

Distributed under the repository's root **[MIT License](./LICENSE)**.

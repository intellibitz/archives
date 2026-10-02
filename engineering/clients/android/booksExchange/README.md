# Books Exchange (`booksExchange`)

**Books Exchange** is a full-stack book discovery, cataloging, and peer-to-peer exchange platform consisting of an Android client application (`bex-droid`) and a Google App Engine cloud backend (`bex-engine`).

---

## Architecture Overview

```
ui/android/booksExchange/
├── client/
│   └── bex-droid/         # Android mobile client
│       ├── AndroidManifest.xml
│       ├── build.xml
│       └── src/com/androidrocks/bex/
│           ├── activity/  # Book shelves, details, adding, and user connections
│           ├── provider/  # BooksProvider SQLite content provider & Google Books API
│           ├── view/      # ShelvesView custom virtual bookshelf UI
│           └── zxing/     # Embedded camera barcode/ISBN scanning engine
└── server/
    └── bex-engine/        # Google App Engine backend
        ├── build.xml      # Ant deployment script
        ├── src/           # Cloud datastore endpoints and sync services
        └── war/           # Web application archive, servlets, and App Engine configs
```

---

## Client Features (`bex-droid`)

- **Barcode & ISBN Scanner**: Embedded ZXing image processing pipeline capable of scanning ISBN barcodes via the device camera (`CaptureActivity`, `DecodeThread`, `QRCodeEncoder`).
- **Google Books Integration**: Automatically queries the Google Books API (`GoogleBooksStore.java`) upon barcode capture to fetch title, author, description, and cover artwork.
- **Virtual Bookshelf Display**: Custom 2D shelf rendering (`ShelvesView.java`) with smooth transitions and spotlight effects.
- **Local Cache & Content Provider**: `BooksProvider` manages local persistence using SQLite and content provider URIs.

---

## Server Architecture (`bex-engine`)

- **Google App Engine Java Runtime**: High-concurrency cloud endpoints serving user book catalogs, exchange requests, and matching.
- **RESTful API**: Serves JSON endpoints consumed by the `bex-droid` mobile client.

---

## Licensing

Distributed under the repository's root **[MIT License](./LICENSE)**.

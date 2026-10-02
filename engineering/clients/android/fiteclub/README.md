# FiteClub (`fiteclub`)

**FiteClub** is a martial arts sparring, matchmaking, and club networking platform featuring a mobile Android client paired with a Google App Engine cloud service.

---

## Directory Structure

```
ui/android/fiteclub/
├── client/
│   └── android/
│       └── fiteclub/      # Android mobile client application
│           ├── AndroidManifest.xml
│           ├── build.xml
│           ├── res/       # Mobile UI layouts and assets
│           ├── src/       # Activities and network service connectors
│           └── tests/     # Client test cases
└── server/
    └── gae/
        ├── doc/           # Design specifications and documentation
        └── fiteclub/      # Google App Engine Python & Django application
            ├── app.yaml   # App Engine service descriptor
            ├── index.yaml # Datastore index configurations
            ├── main.py    # WSGI entrypoint
            ├── manage.py  # Django management script
            ├── settings.py
            ├── fiteclubmodel/ # Data models (Fighters, Clubs, Matches)
            ├── webapi/    # JSON REST endpoints for the Android client
            ├── webui/     # Web dashboard views and handlers
            └── templates/ # Django HTML UI templates
```

---

## Technical Highlights

- **Mobile Client (`client/android/fiteclub`)**:
  - Displays fighter profiles, nearby gyms/dojos, upcoming sparring sessions, and matchmaking feeds.
  - Native communication layer interfacing with the cloud REST API.
- **App Engine Backend (`server/gae/fiteclub`)**:
  - Built on Google App Engine Python runtime with Django framework integration (`appengine_django`).
  - Google Cloud Datastore persistence with declarative indices (`index.yaml`).
  - Dual presentation layer: RESTful endpoints (`webapi/`) for mobile consumption alongside administrative web templates (`webui/`).

---

## Licensing

Distributed under the repository's root **[MIT License](./LICENSE)**.

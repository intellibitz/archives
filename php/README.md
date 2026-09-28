# PHP Web Applications & Toolchains (`os/lang/php/`)

PHP runtime environments, historical web community applications, and interactive AJAX service modules within the **IntelliBitz** ecosystem.

---

## Architecture Overview

```
os/lang/php/
└── intelligeek/       # Full-featured PHP community & interaction web portal
    ├── index.py      # Main application router
    ├── installdb.py  # Database installer and schema initializer
    ├── chat/          # JSON/AJAX asynchronous web chat service
    ├── intellichat/   # Dedicated real-time chatroom module
    ├── askquestion/   # Q&A community knowledge base
    ├── add_question/  # Question submission workflow
    ├── forum/         # Discussion forums and topics
    ├── login/         # User authentication and session handlers
    ├── register/      # Account registration and verification
    ├── newsletter/    # Email broadcast and newsletter engine
    ├── sms_mail/      # SMS and SMTP notification dispatcher
    ├── rssfeed/       # RSS syndication feed generator
    └── game/          # Interactive browser mini-games
```

---

## Key Modules

- **Interactive AJAX Chat (`chat/`, `intellichat/`)**: Real-time polling chat interfaces with JSON payloads, room management, and messaging persistence.
- **Community Knowledge Base (`askquestion/`, `add_question/`)**: Question-and-answer platform enabling users to post technical queries, submit answers, and rank contributions.
- **Notification Services (`sms_mail/`, `newsletter/`)**: Dispatches email newsletters, registration confirmations, and SMS alerts via gateway integration.

---

## Licensing

Distributed under the repository's root **[MIT License](./LICENSE)**.

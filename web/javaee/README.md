# Java EE Enterprise Applications (`web/javaee/`)

Enterprise Java EE multi-module architectures, distributed business services, and cloud-integrated platforms within the **IntelliBitz** ecosystem.

---

## Directory Overview

```
web/javaee/
├── intellidocs/       # Multi-module Maven enterprise document management platform
│   ├── pom.xml        # Root multi-module parent POM
│   ├── intellidocs-ear/   # Enterprise Application Archive packaging
│   ├── intellidocs-ejb/   # Business logic, entity beans, and session services
│   ├── intellidocs-war/   # Web tier, JSF controllers, and servlets
│   ├── intellidocs-tests/ # Integration and unit test suite
│   ├── intellibitz-settings/ # Corporate build configuration & checkstyle
│   └── user-guide/        # User manuals and deployment documentation
└── intellimeet/       # Enterprise collaboration and matchmaking system
    ├── 32tango/       # JBoss Seam / EJB 3 enterprise backend (EAR, EJB, WAR)
    └── dating/        # Companion mobile Android client
```

---

## Systems Architecture

### 1. [IntelliDocs](./web/javaee/intellidocs)

A multi-tier Java EE document management and collaboration solution engineered for modular enterprise deployment:

- **`intellidocs-ejb`**: Implements container-managed persistence, declarative security, transaction demarcations, and core document lifecycle management.
- **`intellidocs-war`**: Delivers a rich web interface using JavaServer Faces (JSF), servlets, and asynchronous listeners.
- **`intellidocs-ear`**: Packages the EJB and WAR components into a unified deployable enterprise archive (`.ear`) for GlassFish, JBoss/WildFly, or WebLogic runtimes.
- **`intellidocs-tests`**: Automated integration tests validating service layer contracts and data persistence.

#### Building IntelliDocs

```bash
cd web/javaee/intellidocs
mvn clean install
```

### 2. [IntelliMeet](./web/javaee/intellimeet)

An enterprise matchmaking and collaborative meeting suite:

- **`32tango/`**: Backend service built with the JBoss Seam framework, uniting Enterprise JavaBeans 3 (EJB 3), Hibernate ORM, and JSF into a cohesive stateful application server architecture.
- **`dating/`**: Companion Android mobile client interfacing with the backend for mobile user coordination and messaging.

---

## Licensing

Distributed under the repository's root **[MIT License](./LICENSE)**.

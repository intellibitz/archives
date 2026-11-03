# Testing Strategy for Acc

Acc ensures reliability through automated protocol validation and environment auditing.

## 📡 Protocol Integrity Tests
Located in `:common:src:commonTest:kotlin:cc.thevar.acc.protocol.ProtocolTest`.
These tests ensure that the shared data models between the **Control Plane** (Kotlin Gateway) and **Deck** (Compose UI) are binary-compatible and serialization-stable.

To run:
```bash
./gradlew :common:jvmTest
```

## 📦 Sandbox & Deployment Verification
Ensure the Gateway JAR builds and runs correctly:

```bash
./gradlew :gateway:run
```

## 🏗️ Hardware Audit
The Manager performs a universal audit on startup:
- **Java (JDK 21+)** availability.
- **Ollama** availability for local model execution.
- **Network Routing** between the Gateway and AI engines.
*Note: Setup and hardware optimization are host-level tasks and cannot be triggered from the UI for security reasons.*

## 🎨 Visual Parity
UI components are tested via Compose Previews and manual verification across:
- **WasmJs**: Browser-native dashboard.
- **JVM Desktop**: High-performance windowed app.
- **Android**: Mobile cockpit.

## 🛡️ Security & Command Verification
The Gateway unit tests verify that the `CommandHandler` white-list correctly blocks unauthorized commands and routes valid ones.
Located in `:gateway:src:test:kotlin:cc.thevar.acc.service.CommandHandlerTest`.

To run:
```bash
./gradlew :gateway:test --tests "cc.thevar.acc.service.CommandHandlerTest"
```

## 🚀 Service & Provisioning Tests
Unit tests for core service logic, including hardware-aware layer calculation and fleet management. These tests also verify correct resource disposal and coroutine cancellation via `AutoCloseable` implementation.
Located in `:gateway:src:test:kotlin:cc.thevar.acc.service.ProvisioningServiceTest`.

To run:
```bash
./gradlew :gateway:test --tests "cc.thevar.acc.service.ProvisioningServiceTest"
```

## 🚀 Release Verification
Before a production release, the following workflow is recommended:

1. **Verify local build**: `./gradlew :gateway:shadowJar`
2. **Execute Test Release**: `./gradlew releaseTest`
3. **Smoke Test**: Download the resulting JAR from the GitHub Pre-release and run it in a clean environment.
4. **Promote**: If the smoke test passes, run `./gradlew releaseProduction`.

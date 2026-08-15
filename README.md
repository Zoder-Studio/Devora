# Devora

A serious Android development environment for developers who want control.

Devora lets you create, edit, build, sign, inspect, and run workflows for
Android/Gradle projects entirely on-device — no PC required. It is not a
code editor and not a mobile port of Android Studio.

## Philosophy

> Devora does not pamper the developer. Devora makes the developer capable.

Devora never auto-fixes errors, never silently chooses dependencies or SDK
versions, never hides real build output, and never modifies your project's
Gradle configuration or workflows without you asking for it. You stay in
control of your code and your environment; Devora only provides the tools.

## Status

Actively under construction, built in stages. Current progress:

- [x] Stage 1 — Core application architecture
- [x] Stage 2 — Project system
- [x] Stage 3 — Project File Manager
- [x] Stage 4 — Terminal sandbox (dual engine: Termux app or embedded bootstrap)
- [x] Stage 5 — Nano integration
- [x] Stage 6 — SDK Manager
- [x] Stage 7 — Gradle Manager
- [x] Stage 8 — Build system
- [x] Stage 9 — Workflow system
- [x] Stage 10 — Per-workflow environment
- [x] Stage 11 — Workflow permissions
- [x] Stage 12 — Artifact Manager
- [x] Stage 13 — APK/AAB Inspector
- [x] Stage 14 — Signing
- [x] Stage 15 — Git
- [x] Stage 16 — GitHub integration
- [x] Stage 17 — Secrets
- [x] Stage 18 — Notifications
- [x] Stage 19 — Plugin system
- [x] Stage 20 — DPAT / account security

## Requirements

- Android 15+ (minSdk 26 for earlier compatibility during development)
- Optional: [Termux](https://github.com/termux/termux-app) installed from
  F-Droid or GitHub releases (not Play Store) for the Termux-app terminal
  engine. If Termux is not installed, Devora falls back to its own
  embedded terminal engine automatically — see [Terminal engines](#terminal-engines).

## Architecture

```Code
Devora
├── app                    Application entry point, navigation, DI wiring
├── core
│   ├── core-common        Result types, dispatchers
│   ├── core-ui             Material 3 theme (Catppuccin Mocha)
│   └── core-logging        Structured logging (never logs secrets/tokens)
└── feature
├── project-manager     Project registry, .devora metadata
├── file-manager         Browse/create/rename/move/copy/delete
├── terminal             Dual-engine terminal (Termux app / embedded)
├── sdk-manager          For SDK System
├── gradle-manager       Gradle managment
├── build-system         Built-in build system
├── workflow-engine      Built-in Workflows for user like Github
├── artifact-manager     Artifact saver on sidebar
├── apk-inspector        Inspect APK and Aab
├── signing              Sign-In Methode
├── git                  Git system
├── github               Github integration
├── secrets              Secrets managment
├── notifications        Android Notification
├── plugin-system        Plugin system for Devora
└── account-security     Using DPAT and secure system
```

Each feature module owns a clear boundary; modules do not reach into each
other's internals. Cross-module coordination goes through domain
interfaces (see `feature/file-manager`'s `OpenTerminalAtPathAction` for an
example of how File Manager delegates terminal launching to `feature/terminal`
without depending on it directly).

## Terminal engines

Devora supports two terminal engines and selects between them automatically:

1. **Termux app (preferred)** — dispatches commands to a separately
   installed Termux app via the `RUN_COMMAND` intent. No GPL code is
   linked into Devora's own binary in this mode.
2. **Embedded bootstrap (fallback)** — when Termux is not installed,
   Devora extracts its own Termux-compatible bootstrap into its own
   app-private `PREFIX` and renders sessions using the official
   `com.termux:terminal-view` / `com.termux:terminal-emulator` libraries.

Because the embedded engine links GPLv3-licensed code
(`com.termux:terminal-view`), any Devora build that includes it is a
combined work under the GNU General Public License v3.0. See
[`feature/terminal/THIRD_PARTY_NOTICES.md`](feature/terminal/THIRD_PARTY_NOTICES.md)
for details, and the `LICENSE` file at the repository root for the full
license text.

## Building

build your own devora app using this command on your terminal:

```Bash
./gradlew :app:assembleDebug
```

Requires JDK 17 and the Android SDK. See individual `build.gradle.kts`
files for per-module dependencies.

## Testing

```Bash
./gradlew test
```

Instrumented tests (device/emulator required, e.g. for Termux integration)
run via:

```Bash
./gradlew connectedAndroidTest
```

## License

GNU General Public License v3.0. See [`LICENSE`](./LICENSE) for the full text.

Third-party components and their licenses are documented per-module in
`THIRD_PARTY_NOTICES.md` files (currently: `feature/terminal/THIRD_PARTY_NOTICES.md`).

## Contributing

This project is under active, staged development following a fixed
design specification. Please open an issue to discuss significant
changes before submitting a pull request that alters established
architecture or behavior decisions.
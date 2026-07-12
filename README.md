# Klikor IDE Bridge

JetBrains IDE companion plugin for [Klikor](https://klikor.io) — the software deck for
controlling your desktop, IDEs, and streaming tools from any device.

Once installed in your IDE, this plugin runs a small local HTTP action server that lets
the Klikor desktop app trigger any IDE action from a Klikor button: Run, Debug, Stop,
Step Over / Into / Out, Resume, Pause, VCS update, Search Everywhere — or any action ID
you find via **Help → Open Action Browser**.

Works across IntelliJ-based IDEs (2024.3+): IntelliJ IDEA, WebStorm, PyCharm, Rider,
DataGrip, GoLand, CLion, Android Studio, DataSpell.

## Install

- **JetBrains Marketplace** (when listed): search for "Klikor IDE Bridge" in
  *Settings → Plugins → Marketplace*.
- **From disk**: download the plugin .zip from the
  [latest Klikor release](https://github.com/NovemberFalls/Klikor/releases/latest),
  then *Settings → Plugins → ⚙ → Install Plugin from Disk…* and restart the IDE.

Then in the Klikor desktop app: *Settings → Developer → JetBrains IDE → Connect*.

## Configuration

*Settings → Tools → Klikor IDE Bridge* in your IDE:

- **Password** (optional) — set the same value in Klikor's JetBrains card.
- **Perform actions only when IDE window is focused** — off by default in this plugin,
  so Klikor buttons work even while the Klikor window has focus.
- **Remote server** (optional, off by default) — listens on port 21420 for control from
  another machine. Leave off unless you need it.

## Version ladder

Klikor IDE Bridge publishes as three parallel JetBrains Marketplace release channels, mirroring
upstream's own release history (upstream's main branch picked up a platform-262-only API partway
through 2026 — see [NOTICE](NOTICE) for the exact commits and compatibility findings):

| Version | Build range        | Upstream base commit                      | Toolchain |
|---------|---------------------|--------------------------------------------|-----------|
| 1.0.0   | 243 – 251.*         | `db61122` ("Release 2025.1 version")       | JDK 21    |
| 1.1.0   | 252 – 261.*         | `9005402` (upstream's "2026.1" release)    | JDK 21    |
| 1.2.0   | 262+ (open-ended)   | `9487e19` ("add claude code action", main HEAD) | JDK 25 |

This working tree carries the **1.2.0** source state (the current `idea-plugin/` subdirectory). 1.1.0's
source is not checked into this repo as a second tree; its built artifact is kept at
[`dist/klikor-ide-bridge-1.1.0.zip`](dist/klikor-ide-bridge-1.1.0.zip) for release/upload convenience,
and is reproducible from its upstream base commit plus the same rebrand file list — see
[NOTICE](NOTICE) for the exact recipe. 1.0.0's source lives in this repo's git history (see the commit
that first added `klikor-jetbrains-plugin/`).

## Build from source

Requires JDK 21 (Klikor IDE Bridge 1.0.0 and 1.1.0, targeting IDEs 2024.3-2026.1) or JDK 25 (Klikor IDE
Bridge 1.2.0+, targeting IDEs 2026.2 and newer — matches upstream's `jvmToolchain(25)` as of the
platform-262 compatibility fix).

```
cd idea-plugin
./gradlew buildPlugin        # → build/distributions/klikor-ide-bridge-<version>.zip
./gradlew verifyPlugin
```

## Provenance & license

Apache License 2.0 — see [LICENSE.txt](LICENSE.txt).

This plugin is a fork of JetBrains' open-source
[intellij-streamdeck-plugin](https://github.com/JetBrains/intellij-streamdeck-plugin)
(Copyright JetBrains s.r.o., Apache-2.0) with Klikor branding, a changed
focus-gating default, and no protocol changes — see [NOTICE](NOTICE) for details.

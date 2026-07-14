# Klikor IDE Bridge

JetBrains IDE companion plugin for [Klikor](https://klikor.io) — the software deck for
controlling your desktop, IDEs, and streaming tools from any device.

Once installed in your IDE, this plugin runs a small local HTTP action server that lets
the Klikor desktop app trigger any IDE action from a Klikor button: Run, Debug, Stop,
Step Over / Into / Out, Resume, Pause, VCS update, Search Everywhere — or any other IDE
action ID, including ones contributed by third-party plugins. The Klikor desktop app ships
curated action presets for common IDE actions, and any action id works.

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

- **Enabled** — turn the local action server on/off. Applying takes effect immediately, no
  IDE restart required.
- **Port** — 0 (default) auto-scans 21420-21430 for the first free port; a fixed value pins
  that exact port.
- **Password** (optional) — set the same value in Klikor's JetBrains card.
- **Perform actions only when IDE window is focused** — off by default, so Klikor buttons
  work even while the Klikor window has focus.
- **Status** — shows `Running on 127.0.0.1:<port>` or `Stopped`.

## Version

**2.0.0** supports all IDEs **2024.3 and newer**, in a single Marketplace artifact — a ground-up
rewrite on stable, public IntelliJ Platform API only (zero internal API usage). It replaces the old
1.0.0/1.1.0/1.2.0 build-range ladder below. It's a dynamic plugin: install, enable, and disable without
an IDE restart. It serves on a dedicated localhost port (21420-21430) rather than the IDE's built-in web
server; Klikor 2.8.1+ auto-discovers it, and legacy 1.x installs still work with Klikor too. See
[NOTICE](NOTICE) for the full rewrite rationale and attribution.

### Legacy versions

Prior to 2.0.0, Klikor IDE Bridge published as three parallel JetBrains Marketplace release channels,
each forked directly from an upstream `intellij-streamdeck-plugin` commit (mirroring upstream's own
release history — upstream's main branch picked up a platform-262-only API partway through 2026):

| Version | Build range        | Upstream base commit                      | Toolchain |
|---------|---------------------|--------------------------------------------|-----------|
| 1.0.0   | 243 – 251.*         | `db61122` ("Release 2025.1 version")       | JDK 21    |
| 1.1.0   | 252 – 261.*         | `9005402` (upstream's "2026.1" release)    | JDK 21    |
| 1.2.0   | 262 – open-ended    | `9487e19` ("add claude code action", main HEAD) | JDK 25 |

1.1.0's source is not checked into this repo as a second tree; its built artifact is kept at
[`dist/klikor-ide-bridge-1.1.0.zip`](dist/klikor-ide-bridge-1.1.0.zip) for release/upload convenience.
1.0.0's and 1.2.0's source live in this repo's git history — see [NOTICE](NOTICE) for the exact commits
and rebuild recipe.

## Build from source

Requires JDK 25 (matches this module's `jvmToolchain(25)`).

```
cd idea-plugin
./gradlew buildPlugin        # → build/distributions/klikor-ide-bridge-<version>.zip
./gradlew verifyPlugin
```

## Provenance & license

Apache License 2.0 — see [LICENSE.txt](LICENSE.txt).

Klikor IDE Bridge 2.0.0 is an independent, ground-up implementation on stable, public IntelliJ Platform
API, retaining the same local HTTP action-execution protocol as, and attribution to, JetBrains'
open-source [intellij-streamdeck-plugin](https://github.com/JetBrains/intellij-streamdeck-plugin)
(Copyright JetBrains s.r.o., Apache-2.0), from which earlier 1.x versions of this plugin were directly
forked — see [NOTICE](NOTICE) for details.

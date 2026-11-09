# Anti-AFK and Auto-Reconnect Mod

A client-side Fabric mod for timed anti-AFK actions, reconnecting to a configured Minecraft server, and replying to player chat with Auto Welcome.

The mod's settings and feature logic run locally on your device. The mod itself makes no connections to external services and uses no VPS, remote control server, telemetry, or analytics. During normal Minecraft use, the game client connects to Microsoft/Mojang services for account and session functions and to the Minecraft server you choose to join.

## Supported Minecraft versions

| Minecraft | Fabric API | Java bytecode target | Project |
| --- | --- | --- | --- |
| 26.3 | 0.165.2+26.3 | 25 | [versions/26.3](versions/26.3) |
| 26.2 | 0.161.0+26.2 | 25 | [versions/26.2](versions/26.2) |
| 26.1.x (verified 26.1–26.1.2) | 0.155.3+26.1.2 | 25 | [versions/26.1](versions/26.1) |
| 1.21.11 | 0.141.6+1.21.11 | 21 | [versions/1.21.11](versions/1.21.11) |
| 1.21.4 | 0.119.4+1.21.4 | 21 | [versions/1.21.4](versions/1.21.4) |

Each version is an independent Gradle project with its own dependencies, mod metadata, resources, and Minecraft-specific client code. Shared configuration models and storage live under `shared/`. The root Gradle project builds all five versions. The 26.1.x module is compiled against 26.1.2 and has been built against 26.1, 26.1.1, and 26.1.2 with their matching Fabric API releases.

## Features

- Open the settings screen with a rebindable key under **Options → Controls**.
- Enable Jump, Hit, Crouch, and Move independently. Each action has its own timer and can overlap with the others.
- Set an initial anti-AFK delay before the first action.
- Jump holds its normal key mapping for one client input tick.
- Hit holds the normal attack key for one second. Minecraft applies its current crosshair target and block occlusion through its ordinary attack handling.
- Crouch and Move each have an interval and a hold duration. A duration cannot exceed its interval.
- Edit the movement sequence. The default is `FORWARD, LEFT, RIGHT, LEFT`.
- Reconnect to a configured address after an unexpected disconnect, with separate first and later retry delays.
- After an automatic reconnect succeeds, send up to five configured chat messages or slash commands in order, one second apart.

Settings are stored locally in `config/anti-afk-auto-reconnect.json`. All features are disabled by default; Auto Respawn starts with a five-second delay and Auto Logout starts with a ten-block radius.

## Build

Run `./gradlew build` from the repository root to build all five JARs. Fabric Loom 1.19.4 requires Java 25 to run Gradle. The 1.21.4 and 1.21.11 artifacts are compiled for Java 21; all 26.x artifacts are compiled for Java 25.

The artifacts are written to:

- `versions/26.3/build/libs/`
- `versions/26.2/build/libs/`
- `versions/26.1/build/libs/`
- `versions/1.21.11/build/libs/`
- `versions/1.21.4/build/libs/`

GitHub Actions builds all five version projects on pushes to `main` and pull requests.

## GitHub Releases

Each Minecraft target has its own GitHub Release with the matching JAR attached. Push a tag in the form `minecraft-<minecraft-version>-v<mod-version>`; the version must match that module's `mod_version` in `gradle.properties`. The release workflow builds and validates only the matching module before publishing the JAR.

Push one release tag at a time. GitHub does not create push events when more than three tags are pushed together.

The initial releases use these tags:

- `minecraft-1.21.4-v0.1.0`
- `minecraft-1.21.11-v0.1.0`
- `minecraft-26.1-v0.1.0`
- `minecraft-26.2-v0.1.0`
- `minecraft-26.3-v0.1.0`

Tag pushes use the versioned release workflow; the regular build workflow does not run for tag pushes.

## Network and input behavior

The mod's configuration and feature logic run locally. Its reconnect and chat actions use Minecraft's normal client connection; the mod does not open a separate connection to a third-party service. The Minecraft client communicates with Microsoft/Mojang services as needed for account and session functions and with the Minecraft server you choose to join. The mod uses ordinary key mappings, the vanilla reconnect flow, and vanilla chat or command submission. It does not construct or reorder gameplay packets. Auto Welcome and post-reconnect chat or command lines share a client-tick gate, so the mod dispatches at most one of those chat or command actions per client tick.

## Reuse and credit

Forking, copying, modifying, and redistributing this project is allowed under the MIT License. Keep the copyright and license notice with copies or substantial portions of the software, and credit the original project in your derivative project's README or About page:

> Based on [Anti-AFK and Auto-Reconnect Mod](https://github.com/2q9e/anti-afk-auto-reconnect-mod) by [@2q9e](https://github.com/2q9e).

See [LICENSE](LICENSE) for the full license text.

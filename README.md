# Anti-AFK and Auto-Reconnect Mod

A client-side Fabric mod for timed anti-AFK actions and reconnecting to a configured Minecraft server.

## Target versions

- Minecraft Java Edition 26.3
- Fabric Loader 0.20.1
- Java 25 or newer

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
- Auto Welcome checks player chat for the substring `welcome`, ignoring letter case and word boundaries. It skips system messages and your own messages, waits the configured delay before replying `welcome`, then waits the same delay before accepting another match.

Auto Welcome and settings are stored locally in `config/anti-afk-auto-reconnect.json`. All features are disabled by default. Reconnect messages and Auto Welcome share a per-tick send gate, so the mod sends no more than one chat or command action on a client tick.

## Build

Run `./gradlew build` on a Java 25 installation. The built mod JAR is in `build/libs`.

## Reuse and credit

Forking, copying, modifying, and redistributing this project is allowed under the MIT License. Keep the copyright and license notice with copies or substantial portions of the software, and credit the original project in your derivative project's README or About page:

> Based on [Anti-AFK and Auto-Reconnect Mod](https://github.com/2q9e/anti-afk-auto-reconnect-mod) by [@2q9e](https://github.com/2q9e).

See [LICENSE](LICENSE) for the full license text.

# Behavior specification

Target platforms: Minecraft Java Edition 26.1.x (compiled against 26.1.2; tested with 26.1, 26.1.1, and 26.1.2), Minecraft Java Edition 26.2, and Minecraft Java Edition 26.3 (Fabric Loader 0.20.1 and Java 25); Minecraft Java Edition 1.21.11 (Fabric Loader 0.19.3 and Java 21); and Minecraft Java Edition 1.21.4 (Fabric Loader 0.20.1 and Java 21). All five version modules follow the behavior below; Minecraft-facing code and dependency pins are maintained separately per version.

## Controls and settings

- A rebindable key opens the configuration screen.
- Settings are saved locally in the Minecraft config directory.
- Reconnect and anti-AFK features are disabled until enabled by the player.

## Reconnect

- Store one configured server address, a first retry delay, and a subsequent retry delay.
- Start retry scheduling after a play disconnect produces the vanilla disconnect screen; a return to the title screen cancels the retry flow.
- Wait the first delay before the first retry, then use the subsequent delay for later failed retries.
- Schedule no more than one retry or connection attempt at a time.
- After an automatic reconnect succeeds, send up to five configured, nonblank chat lines or commands once, in their listed order.
- Lines beginning with `/` are commands; other lines are regular chat. Send each line through the normal client chat or command path, one second apart.

## Auto Welcome

- Auto Welcome is disabled by default and has a configurable delay from 1 to 3600 seconds.
- Inspect signed player chat and general game messages, including server system messages and command feedback. Match the substring `welcome` case-insensitively, even when it appears inside a longer string.
- Ignore player messages sent by the local player.
- For the first accepted match, wait the configured delay, then send exactly `welcome` through the normal client chat path.
- Ignore matches while a reply is pending. After sending a reply, wait the same delay before accepting another match; do not queue matches during that interval.
- Ignore chat received while a screen or overlay is open. If one opens after a match is accepted, pause the pending reply delay until gameplay resumes; the post-reply cooldown continues to count client ticks.
- Auto Welcome and post-reconnect chat or command lines share a per-client-tick send gate. At most one of these mod-initiated chat or command actions is dispatched in a client tick.

## Auto Respawn

- Auto Respawn is disabled by default and has a configurable delay from 0 to 3600 seconds.
- Start a fresh delay whenever the death screen appears. After it expires, send one normal client respawn request. Do not repeat the request while waiting for the server to respawn the player.
- Reset the timer after respawning, leaving the world, or disabling Auto Respawn so every death receives its own delay.

## Auto Logout

- Auto Logout is disabled by default and has a configurable detection radius from 1 to 15 blocks.
- Watch loaded client-side players around the local player. If a non-ignored player enters the radius, disconnect through the normal client flow once.
- Allow up to ten ignored usernames. Match them case-insensitively and require valid 3-to-16-character Minecraft usernames.
- Mark the disconnect as mod-initiated before disconnecting. Auto Reconnect clears pending retries and stops; a later genuine disconnect starts a new decision.

## Anti-AFK

- Wait the configured start delay before the first action.
- Jump, Hit, Crouch, and Move use independent timers and can overlap.
- Jump is a single tap when its interval expires.
- Hit invokes Minecraft's normal attack routine exactly once when its interval expires. It does not hold the attack key between actions, preventing repeated attack packets. Minecraft's current hit result, item checks, target selection, and block occlusion decide the action.
- Crouch and Move each have an interval and a hold duration. A hold duration must not exceed its interval.
- Move follows an editable sequence; the initial sequence is forward, left, right, left.
- When automation is disabled, the player leaves the world, or a screen or overlay blocks gameplay, release automated keys. While gameplay is blocked, clear pending attack clicks so they cannot fire after resuming.
- After a blocking screen or overlay closes, restart the full configured start delay and begin the movement sequence from its first direction.
- Saving a changed Anti-AFK setting restarts all enabled action timers and applies the full start delay.

## Input and connection handling

- Use ordinary client key mappings and the normal client interaction flow.
- Do not construct or reorder gameplay packets for anti-AFK actions.
- Reconnect and chat/command sends use vanilla client connection and submission paths.

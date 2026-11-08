# Behavior specification

Target platform: Minecraft Java Edition 26.3, Fabric Loader 0.20.1, Java 25 or newer.

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
- Inspect player-sent signed chat text only. Match the substring `welcome` case-insensitively, even when it appears inside a longer string.
- Ignore system messages and messages sent by the local player.
- For the first accepted match, wait the configured delay, then send exactly `welcome` through the normal client chat path.
- Ignore matches while a reply is pending. After sending a reply, wait the same delay before accepting another match; do not queue matches during that interval.
- Auto Welcome and post-reconnect chat or command lines share a per-client-tick send gate. At most one of these mod-initiated chat or command actions is dispatched in a client tick.

## Anti-AFK

- Wait the configured start delay before the first action.
- Jump, Hit, Crouch, and Move use independent timers and can overlap.
- Jump is a single tap when its interval expires.
- Hit is held for one second when its interval expires and follows the current vanilla left-click target: air, a block, or an entity. Normal game targeting and block occlusion decide what is hit.
- Crouch and Move each have an interval and a hold duration. A hold duration must not exceed its interval.
- Move follows an editable sequence; the initial sequence is forward, left, right, left.
- When automation is disabled, the player leaves the world, or a screen or overlay blocks gameplay, release automated keys and discard queued automated input clicks.
- After a blocking screen or overlay closes, restart the full configured start delay and begin the movement sequence from its first direction.

## Input and connection handling

- Use ordinary client key mappings and the normal client interaction flow.
- Do not construct or reorder gameplay packets for anti-AFK actions.
- Reconnect and chat/command sends use vanilla client connection and submission paths.

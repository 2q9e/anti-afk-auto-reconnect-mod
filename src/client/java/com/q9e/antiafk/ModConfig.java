package com.q9e.antiafk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class ModConfig {
    private static final Gson COPY_GSON = new GsonBuilder().create();

    public int schemaVersion = 1;
    public ReconnectSettings reconnect = new ReconnectSettings();
    public AntiAfkSettings antiAfk = new AntiAfkSettings();
    public List<String> afterReconnectMessages = new ArrayList<>(
        Collections.nCopies(5, "")
    );

    public ModConfig copy() {
        return COPY_GSON.fromJson(COPY_GSON.toJson(this), ModConfig.class);
    }

    public String validationError() {
        if (schemaVersion != 1) {
            return "Unsupported configuration version.";
        }
        if (reconnect == null || antiAfk == null) {
            return "Reconnect and anti-AFK settings are required.";
        }
        if (reconnect.serverAddress == null || reconnect.serverAddress.length() > 255) {
            return "Server address must contain at most 255 characters.";
        }
        if (reconnect.enabled && reconnect.serverAddress.isBlank()) {
            return "Enter a server address before enabling auto-reconnect.";
        }
        if (!inRange(reconnect.firstRetryDelaySeconds, 0, 3600)
            || !inRange(reconnect.subsequentRetryDelaySeconds, 0, 3600)) {
            return "Reconnect delays must be between 0 and 3600 seconds.";
        }
        if (!inRange(antiAfk.startDelaySeconds, 0, 86400)) {
            return "Anti-AFK start delay must be between 0 and 86400 seconds.";
        }
        if (antiAfk.jump == null || antiAfk.hit == null || antiAfk.crouch == null || antiAfk.move == null) {
            return "All four anti-AFK actions must have settings.";
        }
        if (!inRange(antiAfk.jump.intervalSeconds, 1, 86400)
            || !inRange(antiAfk.hit.intervalSeconds, 1, 86400)) {
            return "Jump and Hit intervals must be between 1 and 86400 seconds.";
        }
        if (!validTimedAction(antiAfk.crouch) || !validTimedAction(antiAfk.move)) {
            return "Crouch and Move durations must be at least 1 second and no greater than their intervals.";
        }
        if (antiAfk.movementOrder == null || antiAfk.movementOrder.isEmpty()) {
            return "Movement order must contain at least one direction.";
        }
        if (antiAfk.movementOrder.stream().anyMatch(direction -> direction == null)) {
            return "Movement order contains an unknown direction.";
        }
        if (afterReconnectMessages == null || afterReconnectMessages.size() > 5) {
            return "Configure no more than five post-reconnect chat lines.";
        }
        if (afterReconnectMessages.stream().anyMatch(line -> line != null && line.length() > 256)) {
            return "Each post-reconnect line must be 256 characters or fewer.";
        }
        return null;
    }

    private static boolean validTimedAction(TimedActionSettings action) {
        return inRange(action.intervalSeconds, 1, 86400)
            && inRange(action.durationSeconds, 1, action.intervalSeconds);
    }

    private static boolean inRange(int value, int minimum, int maximum) {
        return value >= minimum && value <= maximum;
    }
}

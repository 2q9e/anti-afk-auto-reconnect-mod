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
        if (!reconnect.serverAddress.isBlank() && !isValidServerAddress(reconnect.serverAddress)) {
            return "Enter a host or IP address, with an optional valid port.";
        }
        if (!inRange(reconnect.firstRetryDelaySeconds, 1, 3600)
            || !inRange(reconnect.subsequentRetryDelaySeconds, 1, 3600)) {
            return "Reconnect delays must be between 1 and 3600 seconds.";
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
        if (antiAfk.movementOrder == null || antiAfk.movementOrder.isEmpty()
            || antiAfk.movementOrder.size() > 16) {
            return "Movement order must contain between 1 and 16 directions.";
        }
        if (antiAfk.movementOrder.stream().anyMatch(direction -> direction == null)) {
            return "Movement order contains an unknown direction.";
        }
        if (afterReconnectMessages == null || afterReconnectMessages.size() > 5) {
            return "Configure no more than five post-reconnect chat lines.";
        }
        if (afterReconnectMessages.stream().anyMatch(line -> line == null
            || line.length() > 256 || line.chars().anyMatch(Character::isISOControl))) {
            return "Each post-reconnect line must be single-line text of 256 characters or fewer.";
        }
        if (afterReconnectMessages.stream().anyMatch(line -> line.startsWith("/")
            && line.substring(1).isBlank())) {
            return "A slash command must include text after the slash.";
        }
        return null;
    }

    private static boolean isValidServerAddress(String value) {
        String address = value.trim();
        if (address.isEmpty() || address.chars().anyMatch(Character::isWhitespace)
            || address.chars().anyMatch(Character::isISOControl)
            || address.contains("/") || address.contains("\\")
            || address.contains("://")) {
            return false;
        }

        String host;
        String port = null;
        if (address.startsWith("[")) {
            int closingBracket = address.indexOf(']');
            if (closingBracket <= 1) {
                return false;
            }
            host = address.substring(1, closingBracket);
            String suffix = address.substring(closingBracket + 1);
            if (!suffix.isEmpty()) {
                if (!suffix.startsWith(":")) {
                    return false;
                }
                port = suffix.substring(1);
            }
            if (!host.contains(":") || !host.matches("[0-9A-Fa-f:.%_-]+")) {
                return false;
            }
        } else {
            long colonCount = address.chars().filter(character -> character == ':').count();
            if (colonCount == 1) {
                int colon = address.indexOf(':');
                host = address.substring(0, colon);
                port = address.substring(colon + 1);
            } else {
                host = address;
                if (colonCount > 1 && !host.matches("[0-9A-Fa-f:.%_-]+")) {
                    return false;
                }
            }
            if (host.isEmpty()) {
                return false;
            }
            if (colonCount <= 1 && !host.matches("[A-Za-z0-9._-]+")) {
                return false;
            }
        }

        if (port != null) {
            if (port.isEmpty() || !port.chars().allMatch(Character::isDigit)) {
                return false;
            }
            try {
                int number = Integer.parseInt(port);
                return inRange(number, 1, 65535);
            } catch (NumberFormatException exception) {
                return false;
            }
        }
        return true;
    }

    private static boolean validTimedAction(TimedActionSettings action) {
        return inRange(action.intervalSeconds, 1, 86400)
            && inRange(action.durationSeconds, 1, action.intervalSeconds);
    }

    private static boolean inRange(int value, int minimum, int maximum) {
        return value >= minimum && value <= maximum;
    }
}

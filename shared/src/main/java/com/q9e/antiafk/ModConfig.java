package com.q9e.antiafk;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class ModConfig {
    private static final Gson COPY_GSON = new GsonBuilder().create();

    public int schemaVersion = 1;
    public ReconnectSettings reconnect = new ReconnectSettings();
    public AntiAfkSettings antiAfk = new AntiAfkSettings();
    public AutoWelcomeSettings autoWelcome = new AutoWelcomeSettings();
    public AutoRespawnSettings autoRespawn = new AutoRespawnSettings();
    public AutoLogoutSettings autoLogout = new AutoLogoutSettings();
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
        if (reconnect == null || antiAfk == null || autoWelcome == null
            || autoRespawn == null || autoLogout == null) {
            return "Reconnect, anti-AFK, auto-welcome, auto-respawn, and auto-logout settings are required.";
        }
        if (!inRange(autoRespawn.delaySeconds, 0, 3600)) {
            return "Auto Respawn delay must be between 0 and 3600 seconds.";
        }
        if (!inRange(autoLogout.detectionRangeBlocks, 1, 15)) {
            return "Auto Logout detection range must be between 1 and 15 blocks.";
        }
        if (autoLogout.ignoredUsernames == null || autoLogout.ignoredUsernames.size() > 10) {
            return "Auto Logout can ignore no more than ten usernames.";
        }
        Set<String> ignoredUsernames = new HashSet<>();
        for (String username : autoLogout.ignoredUsernames) {
            if (username == null || !username.matches("[A-Za-z0-9_]{3,16}")) {
                return "Ignored usernames must contain 3 to 16 letters, numbers, or underscores.";
            }
            if (!ignoredUsernames.add(username.toLowerCase(Locale.ROOT))) {
                return "Each Auto Logout username can only be listed once.";
            }
        }
        if (!inRange(autoWelcome.delaySeconds, 1, 3600)) {
            return "Auto Welcome delay must be between 1 and 3600 seconds.";
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
            if (!isValidIpv6Literal(host)) {
                return false;
            }
        } else {
            long colonCount = address.chars().filter(character -> character == ':').count();
            if (colonCount == 1) {
                int colon = address.indexOf(':');
                host = address.substring(0, colon);
                port = address.substring(colon + 1);
                if (!isValidHost(host)) {
                    return false;
                }
            } else if (colonCount > 1) {
                host = address;
                if (!isValidIpv6Literal(host)) {
                    return false;
                }
            } else {
                host = address;
                if (!isValidHost(host)) {
                    return false;
                }
            }
        }

        return port == null || isValidPort(port);
    }

    private static boolean isValidHost(String value) {
        String host = value;
        if (host.endsWith(".")) {
            host = host.substring(0, host.length() - 1);
        }
        if (host.isEmpty() || host.length() > 253) {
            return false;
        }

        if (host.matches("[0-9.]+")) {
            return isValidIpv4Literal(host);
        }

        String[] labels = host.split("\\.", -1);
        for (String label : labels) {
            if (label.isEmpty() || label.length() > 63
                || !label.matches("[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?")) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidIpv4Literal(String value) {
        String[] octets = value.split("\\.", -1);
        if (octets.length != 4) {
            return false;
        }
        for (String octet : octets) {
            if (octet.isEmpty() || octet.length() > 3
                || !octet.matches("[0-9]+")) {
                return false;
            }
            try {
                if (!inRange(Integer.parseInt(octet), 0, 255)) {
                    return false;
                }
            } catch (NumberFormatException exception) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidIpv6Literal(String host) {
        if (!host.contains(":") || !host.matches("[0-9A-Fa-f:.]+")) {
            return false;
        }

        try {
            // The colon and character checks keep this on Java's numeric IPv6 parser,
            // while supporting both compressed groups and embedded IPv4 tails.
            InetAddress.getByName(host);
            return true;
        } catch (UnknownHostException exception) {
            return false;
        }
    }
    private static boolean isValidPort(String port) {
        if (port.isEmpty() || !port.matches("[0-9]+")) {
            return false;
        }
        try {
            return inRange(Integer.parseInt(port), 1, 65535);
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private static boolean validTimedAction(TimedActionSettings action) {
        return inRange(action.intervalSeconds, 1, 86400)
            && inRange(action.durationSeconds, 1, action.intervalSeconds);
    }

    private static boolean inRange(int value, int minimum, int maximum) {
        return value >= minimum && value <= maximum;
    }
}

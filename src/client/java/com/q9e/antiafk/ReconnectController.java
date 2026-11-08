package com.q9e.antiafk;

import java.util.ArrayDeque;
import java.util.Queue;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.ServerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ReconnectController {
    private static final Logger LOGGER = LoggerFactory.getLogger("anti_afk_reconnect");
    private static final int TICKS_PER_SECOND = 20;
    private static final int DISCONNECT_SCREEN_GRACE_TICKS = 10;

    private enum State {
        IDLE,
        CHECKING_DISCONNECT,
        WAITING,
        CONNECTING,
        PLAYING
    }

    private final Queue<String> postReconnectMessages = new ArrayDeque<>();
    private State state = State.IDLE;
    private ClientPacketListener playConnection;
    private Screen previousDisconnectScreen;
    private Screen connectScreen;
    private long tick;
    private long retryAt;
    private long nextMessageAt;
    private long disconnectCheckDeadline;
    private int retriesScheduled;
    private boolean automaticAttempt;

    public void onPlayDisconnect(Minecraft client, ReconnectSettings settings) {
        if (state != State.PLAYING) {
            return;
        }

        playConnection = null;
        postReconnectMessages.clear();
        if (settings == null || !settings.enabled) {
            clear();
            return;
        }

        retriesScheduled = 0;
        disconnectCheckDeadline = tick + DISCONNECT_SCREEN_GRACE_TICKS;
        state = State.CHECKING_DISCONNECT;
    }

    public void onPlayJoin(Minecraft client, ClientPacketListener listener, ModConfig config) {
        boolean reconnectedAutomatically = automaticAttempt;
        playConnection = listener;
        previousDisconnectScreen = null;
        connectScreen = null;
        state = State.PLAYING;
        retriesScheduled = 0;
        automaticAttempt = false;
        postReconnectMessages.clear();

        if (reconnectedAutomatically && config != null && config.reconnect.enabled
            && config.afterReconnectMessages != null) {
            config.afterReconnectMessages.stream()
                .filter(line -> line != null && !line.isBlank())
                .limit(5)
                .forEach(postReconnectMessages::add);
            nextMessageAt = tick + TICKS_PER_SECOND;
        }
    }

    public void onSettingsChanged(ReconnectSettings settings) {
        if (settings == null || !settings.enabled) {
            postReconnectMessages.clear();
            if (state != State.PLAYING) {
                clear();
            }
            return;
        }

        if (state == State.WAITING) {
            int delaySeconds = retriesScheduled == 1
                ? settings.firstRetryDelaySeconds
                : settings.subsequentRetryDelaySeconds;
            retryAt = tick + secondsToTicks(delaySeconds);
        }
    }

    public void tick(Minecraft client, ReconnectSettings settings) {
        tick++;
        if (settings == null || !settings.enabled) {
            if (state == State.PLAYING) {
                postReconnectMessages.clear();
            } else {
                clear();
            }
            return;
        }

        if (state == State.CHECKING_DISCONNECT) {
            checkDisconnectScreen(client, settings);
        } else if (state == State.WAITING) {
            if (client.player == null && client.gui.screen() != previousDisconnectScreen) {
                clear();
            } else if (tick >= retryAt) {
                beginReconnect(client, settings);
            }
        } else if (state == State.CONNECTING) {
            inspectConnectionScreen(client, settings);
        }

        sendNextMessage(client);
    }

    private void checkDisconnectScreen(Minecraft client, ReconnectSettings settings) {
        Screen screen = client.gui.screen();
        if (screen instanceof DisconnectedScreen) {
            previousDisconnectScreen = screen;
            scheduleRetry(settings);
        } else if (tick >= disconnectCheckDeadline) {
            clear();
        }
    }

    private void inspectConnectionScreen(Minecraft client, ReconnectSettings settings) {
        Screen screen = client.gui.screen();
        if (screen instanceof DisconnectedScreen && screen != previousDisconnectScreen) {
            previousDisconnectScreen = screen;
            scheduleRetry(settings);
        } else if (screen == previousDisconnectScreen || screen instanceof TitleScreen) {
            clear();
        } else if (screen != connectScreen && screen != null && client.player == null) {
            clear();
        }
    }

    private void beginReconnect(Minecraft client, ReconnectSettings settings) {
        state = State.CONNECTING;
        automaticAttempt = true;

        try {
            String addressText = settings.serverAddress.trim();
            ServerAddress address = ServerAddress.parseString(addressText);
            ServerData server = new ServerData(
                addressText,
                addressText,
                ServerData.Type.OTHER
            );
            previousDisconnectScreen = client.gui.screen();
            ConnectScreen.startConnecting(
                previousDisconnectScreen,
                client,
                address,
                server,
                false,
                null
            );
            connectScreen = client.gui.screen();
        } catch (RuntimeException exception) {
            LOGGER.warn("Could not start reconnect to the configured server address.", exception);
            scheduleRetry(settings);
        }
    }

    private void scheduleRetry(ReconnectSettings settings) {
        int delaySeconds = retriesScheduled == 0
            ? settings.firstRetryDelaySeconds
            : settings.subsequentRetryDelaySeconds;
        retriesScheduled++;
        retryAt = tick + secondsToTicks(delaySeconds);
        state = State.WAITING;
        automaticAttempt = false;
        connectScreen = null;
    }

    private void sendNextMessage(Minecraft client) {
        if (state != State.PLAYING || playConnection == null
            || postReconnectMessages.isEmpty() || tick < nextMessageAt
            || client.player == null || client.level == null
            || client.gui.screen() != null) {
            return;
        }

        String line = postReconnectMessages.remove();
        if (line.startsWith("/")) {
            String command = line.substring(1);
            if (!command.isBlank()) {
                playConnection.sendCommand(command);
            }
        } else if (!line.isBlank()) {
            playConnection.sendChat(line);
        }
        nextMessageAt = tick + TICKS_PER_SECOND;
    }

    private void clear() {
        state = State.IDLE;
        playConnection = null;
        previousDisconnectScreen = null;
        connectScreen = null;
        postReconnectMessages.clear();
        retryAt = 0;
        nextMessageAt = 0;
        disconnectCheckDeadline = 0;
        retriesScheduled = 0;
        automaticAttempt = false;
    }

    private static long secondsToTicks(int seconds) {
        return Math.max(0L, (long) seconds) * TICKS_PER_SECOND;
    }
}

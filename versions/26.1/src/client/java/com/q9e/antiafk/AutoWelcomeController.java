package com.q9e.antiafk;

import java.util.Locale;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

public final class AutoWelcomeController {
    private static final int TICKS_PER_SECOND = 20;
    private static final String REPLY = "welcome";

    private final ClientChatSendGate chatSendGate;
    private long tick;
    private long sendAt;
    private long nextScanAt;
    private boolean pending;
    private ClientPacketListener sessionConnection;

    public AutoWelcomeController(ClientChatSendGate chatSendGate) {
        this.chatSendGate = chatSendGate;
    }

    public void onChatMessage(
        Minecraft client,
        UUID senderId,
        String messageContent,
        AutoWelcomeSettings settings
    ) {
        if (!isEnabledInWorld(client, settings) || messageContent == null
            || (senderId != null && client.player.getUUID().equals(senderId))) {
            return;
        }

        ClientPacketListener connection = client.getConnection();
        if (connection == null) {
            return;
        }
        if (sessionConnection != null && sessionConnection != connection) {
            clear();
        }
        if (pending || tick < nextScanAt
            || !messageContent.toLowerCase(Locale.ROOT).contains(REPLY)) {
            return;
        }

        sessionConnection = connection;
        pending = true;
        sendAt = tick + secondsToTicks(settings.delaySeconds);
    }

    public void tick(Minecraft client, AutoWelcomeSettings settings) {
        tick++;
        if (!isEnabledInWorld(client, settings)) {
            clear();
            return;
        }

        ClientPacketListener connection = client.getConnection();
        if (connection == null) {
            clear();
            return;
        }
        if (sessionConnection != null && sessionConnection != connection) {
            clear();
        }
        if (client.screen != null || client.getOverlay() != null) {
            if (pending) {
                sendAt++;
            }
            return;
        }
        if (!pending || tick < sendAt) {
            return;
        }
        if (sessionConnection != connection) {
            clear();
            return;
        }

        if (chatSendGate.trySendChat(connection, REPLY)) {
            pending = false;
            sendAt = 0;
            nextScanAt = tick + secondsToTicks(settings.delaySeconds);
        }
    }

    public void onSettingsChanged() {
        clear();
    }

    private static boolean isEnabledInWorld(Minecraft client, AutoWelcomeSettings settings) {
        return settings != null && settings.enabled
            && client.player != null && client.level != null;
    }

    private void clear() {
        sendAt = 0;
        nextScanAt = 0;
        pending = false;
        sessionConnection = null;
    }

    private static long secondsToTicks(int seconds) {
        return Math.max(0L, (long) seconds) * TICKS_PER_SECOND;
    }
}
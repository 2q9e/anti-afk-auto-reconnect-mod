package com.q9e.antiafk;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.entity.player.Player;

public final class AutoLogoutController {
    private final ReconnectController reconnectController;
    private ClientPacketListener activeConnection;
    private boolean disconnecting;

    public AutoLogoutController(ReconnectController reconnectController) {
        this.reconnectController = reconnectController;
    }

    public void tick(Minecraft client, AutoLogoutSettings settings) {
        if (settings == null || !settings.enabled || client.player == null
            || client.level == null || !client.player.isAlive()) {
            reset();
            return;
        }

        ClientPacketListener connection = client.getConnection();
        if (connection == null) {
            reset();
            return;
        }
        if (activeConnection != connection) {
            activeConnection = connection;
            disconnecting = false;
        }
        if (disconnecting) {
            return;
        }

        double rangeSquared = (double) settings.detectionRangeBlocks * settings.detectionRangeBlocks;
        for (Player otherPlayer : client.level.players()) {
            if (otherPlayer == client.player
                || otherPlayer.distanceToSqr(client.player) > rangeSquared
                || isIgnored(otherPlayer.getName().getString(), settings.ignoredUsernames)) {
                continue;
            }

            disconnecting = true;
            reconnectController.markModInitiatedDisconnect();
            client.disconnect();
            return;
        }
    }

    public void onSettingsChanged() {
        reset();
    }

    private static boolean isIgnored(String username, List<String> ignoredUsernames) {
        if (ignoredUsernames == null) {
            return false;
        }
        return ignoredUsernames.stream()
            .filter(name -> name != null)
            .anyMatch(name -> name.equalsIgnoreCase(username));
    }

    private void reset() {
        activeConnection = null;
        disconnecting = false;
    }
}

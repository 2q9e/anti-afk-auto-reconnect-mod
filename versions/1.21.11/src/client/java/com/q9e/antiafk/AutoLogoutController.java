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


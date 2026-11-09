package com.q9e.antiafk;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;

public final class AutoRespawnController {
    private static final int TICKS_PER_SECOND = 20;

    private long tick;
    private long respawnAt = -1;
    private Object activeLevel;
    private boolean respawnRequested;

    public void tick(Minecraft client, AutoRespawnSettings settings) {
        tick++;
        if (settings == null || !settings.enabled || client.player == null
            || client.level == null) {
            reset();
            return;
        }

        if (activeLevel != client.level) {
            respawnAt = -1;
            respawnRequested = false;
            activeLevel = client.level;
        }
        if (!(client.gui.screen() instanceof DeathScreen) || client.player.isAlive()) {
            respawnAt = -1;
            respawnRequested = false;
            return;
        }
        if (respawnRequested) {
            return;
        }
        if (respawnAt < 0) {
            respawnAt = tick + secondsToTicks(settings.delaySeconds);
        }
        if (tick >= respawnAt) {
            client.player.respawn();
            respawnRequested = true;
        }
    }

    public void onSettingsChanged() {
        respawnAt = -1;
        respawnRequested = false;
    }

    private void reset() {
        respawnAt = -1;
        activeLevel = null;
        respawnRequested = false;
    }

    private static long secondsToTicks(int seconds) {
        return Math.max(0L, (long) seconds) * TICKS_PER_SECOND;
    }
}

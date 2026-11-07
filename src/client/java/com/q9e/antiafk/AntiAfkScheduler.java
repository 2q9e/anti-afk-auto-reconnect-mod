package com.q9e.antiafk;

import java.util.List;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public final class AntiAfkScheduler {
    public static final int HIT_HOLD_SECONDS = 1;

    private final AutomationInput input = new AutomationInput();
    private long tick;
    private long startAt;
    private long nextJumpAt;
    private long nextHitAt;
    private long hitReleaseAt;
    private long nextCrouchAt;
    private long crouchReleaseAt;
    private long nextMoveAt;
    private long moveReleaseAt;
    private MovementDirection activeMoveDirection;
    private int movementIndex;
    private Object activeLevel;
    private boolean running;
    public void reset(Minecraft client) {
        input.releaseAll();
        activeLevel = null;
        activeMoveDirection = null;
        movementIndex = 0;
        running = false;
    }

    public void tick(Minecraft client, AntiAfkSettings settings) {
        tick++;
        if (settings == null || !settings.enabled || client.player == null
            || client.level == null || client.gui.screen() != null) {
            reset(client);
            return;
        }

        if (activeLevel != client.level) {
            reset(client);
            activeLevel = client.level;
        }
        if (!running) {
            begin(settings);
        }
        if (tick < startAt) {
            return;
        }

        releaseExpiredInputs(client);
        scheduleJump(client, settings);
        scheduleHit(client, settings);
        scheduleCrouch(client, settings);
        scheduleMove(client, settings);
    }

    private void begin(AntiAfkSettings settings) {
        startAt = tick + secondsToTicks(settings.startDelaySeconds);
        nextJumpAt = startAt;
        nextHitAt = startAt;
        nextCrouchAt = startAt;
        nextMoveAt = startAt;
        hitReleaseAt = 0;
        crouchReleaseAt = 0;
        moveReleaseAt = 0;
        activeMoveDirection = null;
        movementIndex = 0;
        running = true;
    }

    private void scheduleJump(Minecraft client, AntiAfkSettings settings) {
        if (!settings.jump.enabled || tick < nextJumpAt) {
            return;
        }

        input.tap(client.options.keyJump);
        nextJumpAt = tick + secondsToTicks(settings.jump.intervalSeconds);
    }

    private void scheduleHit(Minecraft client, AntiAfkSettings settings) {
        KeyMapping attack = client.options.keyAttack;
        if (!settings.hit.enabled || tick < nextHitAt) {
            return;
        }

        if (input.hold(attack)) {
            // Queue the vanilla attack binding; Minecraft resolves the current target,
            // including block occlusion, through its ordinary interaction flow.
            KeyMapping.click(attack.getKey());
            hitReleaseAt = tick + secondsToTicks(HIT_HOLD_SECONDS);
        }
        nextHitAt = tick + secondsToTicks(settings.hit.intervalSeconds);
    }

}

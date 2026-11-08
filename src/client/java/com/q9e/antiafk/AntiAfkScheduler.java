package com.q9e.antiafk;

import java.util.List;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public final class AntiAfkScheduler {
    public static final int HIT_HOLD_SECONDS = 1;
    private static final int TICKS_PER_SECOND = 20;

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
        startAt = 0;
        nextJumpAt = 0;
        nextHitAt = 0;
        hitReleaseAt = 0;
        nextCrouchAt = 0;
        crouchReleaseAt = 0;
        nextMoveAt = 0;
        moveReleaseAt = 0;
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

    private void releaseExpiredInputs(Minecraft client) {
        if (hitReleaseAt > 0 && tick >= hitReleaseAt) {
            input.release(client.options.keyAttack);
            hitReleaseAt = 0;
        }
        if (crouchReleaseAt > 0 && tick >= crouchReleaseAt) {
            input.release(client.options.keyShift);
            crouchReleaseAt = 0;
        }
        if (moveReleaseAt > 0 && tick >= moveReleaseAt) {
            releaseMove(client);
        }
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
            // Vanilla resolves the current crosshair target and block occlusion
            // through its regular attack handling after this key mapping is queued.
            input.queueClick(attack);
            hitReleaseAt = tick + secondsToTicks(HIT_HOLD_SECONDS);
        }
        nextHitAt = tick + secondsToTicks(settings.hit.intervalSeconds);
    }

    private void scheduleCrouch(Minecraft client, AntiAfkSettings settings) {
        KeyMapping crouch = client.options.keyShift;
        if (!settings.crouch.enabled) {
            input.release(crouch);
            crouchReleaseAt = 0;
            return;
        }
        if (tick < nextCrouchAt) {
            return;
        }

        if (input.hold(crouch)) {
            crouchReleaseAt = tick + secondsToTicks(settings.crouch.durationSeconds);
        }
        nextCrouchAt = tick + secondsToTicks(settings.crouch.intervalSeconds);
    }

    private void scheduleMove(Minecraft client, AntiAfkSettings settings) {
        List<MovementDirection> order = settings.movementOrder;
        if (!settings.move.enabled || order == null || order.isEmpty()) {
            releaseMove(client);
            return;
        }
        if (tick < nextMoveAt) {
            return;
        }

        releaseMove(client);
        MovementDirection direction = order.get(movementIndex % order.size());
        movementIndex = (movementIndex + 1) % order.size();
        KeyMapping mapping = keyFor(client, direction);
        if (input.hold(mapping)) {
            activeMoveDirection = direction;
            moveReleaseAt = tick + secondsToTicks(settings.move.durationSeconds);
        }
        nextMoveAt = tick + secondsToTicks(settings.move.intervalSeconds);
    }

    private void releaseMove(Minecraft client) {
        if (activeMoveDirection != null) {
            input.release(keyFor(client, activeMoveDirection));
        }
        activeMoveDirection = null;
        moveReleaseAt = 0;
    }

    private static KeyMapping keyFor(Minecraft client, MovementDirection direction) {
        return switch (direction) {
            case FORWARD -> client.options.keyUp;
            case LEFT -> client.options.keyLeft;
            case RIGHT -> client.options.keyRight;
        };
    }

    private static long secondsToTicks(int seconds) {
        return Math.max(0L, (long) seconds) * TICKS_PER_SECOND;
    }
}

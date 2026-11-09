package com.q9e.antiafk;

import java.util.List;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public final class AntiAfkScheduler {
    private static final int TICKS_PER_SECOND = 20;

    private final AutomationInput input = new AutomationInput();
    private long tick;
    private long startAt;
    private long nextJumpAt;
    private long jumpReleaseAt;
    private long nextHitAt;
    private long nextCrouchAt;
    private long crouchReleaseAt;
    private long nextMoveAt;
    private long moveReleaseAt;
    private MovementDirection activeMoveDirection;
    private int movementIndex;
    private Object activeLevel;
    private boolean running;

    public void reset(Minecraft client) {
        boolean overlayBlocked = client.getOverlay() != null;
        boolean blocked = client.screen != null || client.player == null
            || client.level == null || overlayBlocked;
        if (blocked) {
            if (overlayBlocked) {
                // Vanilla does not clear queued mappings when it installs an overlay.
                KeyMapping.releaseAll();
            }
            while (client.options.keyAttack.consumeClick()) {
                // Discard stale left-clicks that cannot be handled while gameplay is blocked.
            }
        }
        input.releaseAll(!blocked);
        activeLevel = null;
        activeMoveDirection = null;
        movementIndex = 0;
        startAt = 0;
        nextJumpAt = 0;
        jumpReleaseAt = 0;
        nextHitAt = 0;
        nextCrouchAt = 0;
        crouchReleaseAt = 0;
        nextMoveAt = 0;
        moveReleaseAt = 0;
        running = false;
    }

    public void tick(Minecraft client, AntiAfkSettings settings) {
        tick++;
        if (settings == null || !settings.enabled || client.player == null
            || client.level == null || client.screen != null
            || client.getOverlay() != null) {
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
        jumpReleaseAt = 0;
        nextHitAt = startAt;
        nextCrouchAt = startAt;
        nextMoveAt = startAt;
        crouchReleaseAt = 0;
        moveReleaseAt = 0;
        activeMoveDirection = null;
        movementIndex = 0;
        running = true;
    }

    private void releaseExpiredInputs(Minecraft client) {
        if (jumpReleaseAt > 0 && tick >= jumpReleaseAt) {
            input.release(client.options.keyJump);
            jumpReleaseAt = 0;
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

        if (input.tap(client.options.keyJump)) {
            jumpReleaseAt = tick + 1;
        }
        nextJumpAt = tick + secondsToTicks(settings.jump.intervalSeconds);
    }

    private void scheduleHit(Minecraft client, AntiAfkSettings settings) {
        if (!settings.hit.enabled || tick < nextHitAt) {
            return;
        }

        // Send one normal vanilla attack attempt. Holding the attack mapping
        // would let vanilla repeat attacks on every client tick.
        client.startAttack();
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

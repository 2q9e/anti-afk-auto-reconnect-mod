package com.q9e.antiafk;

import java.util.ArrayList;
import java.util.List;

public final class AntiAfkSettings {
    public boolean enabled = false;
    public int startDelaySeconds = 30;
    public TapActionSettings jump = new TapActionSettings();
    public TapActionSettings hit = new TapActionSettings();
    public TimedActionSettings crouch = new TimedActionSettings();
    public TimedActionSettings move = new TimedActionSettings();
    public List<MovementDirection> movementOrder = new ArrayList<>(
        List.of(MovementDirection.FORWARD, MovementDirection.LEFT, MovementDirection.RIGHT, MovementDirection.LEFT)
    );

    public AntiAfkSettings() {
        crouch.intervalSeconds = 60;
        crouch.durationSeconds = 2;
        move.intervalSeconds = 45;
        move.durationSeconds = 3;
    }
}

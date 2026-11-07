package com.q9e.antiafk;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class AntiAfkSettingsScreen extends Screen {
    private final Screen parent;
    private final ModConfig draft;

    private EditBox startDelay;
    private EditBox jumpInterval;
    private EditBox hitInterval;
    private EditBox crouchInterval;
    private EditBox crouchDuration;
    private EditBox moveInterval;
    private EditBox moveDuration;
    private EditBox movementOrder;
    private boolean antiAfkEnabled;
    private boolean jumpEnabled;
    private boolean hitEnabled;
    private boolean crouchEnabled;
    private boolean moveEnabled;
    private String status = "";

    public AntiAfkSettingsScreen(Screen parent, ModConfig draft) {
        super(Component.literal("Anti-AFK settings"));
        this.parent = parent;
        this.draft = draft;
    }

    @Override
    protected void init() {
        AntiAfkSettings settings = draft.antiAfk;
        int left = Math.max(16, width / 2 - 205);

        antiAfkEnabled = settings.enabled;
        jumpEnabled = settings.jump.enabled;
        hitEnabled = settings.hit.enabled;
        crouchEnabled = settings.crouch.enabled;
        moveEnabled = settings.move.enabled;

        addRenderableWidget(toggle(left, 44, 145, "Anti-AFK", () -> antiAfkEnabled, value -> antiAfkEnabled = value));
        startDelay = field(left + 255, 57, 100, settings.startDelaySeconds);

        addRenderableWidget(toggle(left, 101, 130, "Jump", () -> jumpEnabled, value -> jumpEnabled = value));
        jumpInterval = field(left + 160, 114, 86, settings.jump.intervalSeconds);

        addRenderableWidget(toggle(left, 151, 130, "Hit (1 second)", () -> hitEnabled, value -> hitEnabled = value));
        hitInterval = field(left + 160, 164, 86, settings.hit.intervalSeconds);

        addRenderableWidget(toggle(left, 201, 130, "Crouch", () -> crouchEnabled, value -> crouchEnabled = value));
        crouchInterval = field(left + 160, 214, 86, settings.crouch.intervalSeconds);
        crouchDuration = field(left + 270, 214, 86, settings.crouch.durationSeconds);

        addRenderableWidget(toggle(left, 251, 130, "Move", () -> moveEnabled, value -> moveEnabled = value));
        moveInterval = field(left + 160, 264, 86, settings.move.intervalSeconds);
        moveDuration = field(left + 270, 264, 86, settings.move.durationSeconds);

        movementOrder = new EditBox(font, left, 319, 356, 20, Component.literal("Movement order"));
        movementOrder.setMaxLength(256);
        movementOrder.setValue(String.join(", ", settings.movementOrder.stream().map(Enum::name).toList()));
        addRenderableWidget(movementOrder);

        addRenderableWidget(Button.builder(Component.literal("Apply"), button -> apply())
            .bounds(width / 2 - 105, height - 38, 100, 24).build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> minecraft.gui.setScreen(parent))
            .bounds(width / 2 + 5, height - 38, 100, 24).build());
    }

    private Button toggle(
        int x,
        int y,
        int buttonWidth,
        String label,
        BooleanSupplier read,
        Consumer<Boolean> write
    ) {
        Button[] reference = new Button[1];
        reference[0] = Button.builder(toggleMessage(label, read.getAsBoolean()), button -> {
            boolean nextValue = !read.getAsBoolean();
            write.accept(nextValue);
            button.setMessage(toggleMessage(label, nextValue));
        }).bounds(x, y, buttonWidth, 22).build();
        return reference[0];
    }

    private EditBox field(int x, int y, int fieldWidth, int value) {
        EditBox field = new EditBox(font, x, y, fieldWidth, 20, Component.empty());
        field.setMaxLength(8);
        field.setValue(Integer.toString(value));
        addRenderableWidget(field);
        return field;
    }

    private void apply() {
        try {
            AntiAfkSettings updated = new AntiAfkSettings();
            updated.enabled = antiAfkEnabled;
            updated.startDelaySeconds = parseInteger(startDelay);
            updated.jump.enabled = jumpEnabled;
            updated.jump.intervalSeconds = parseInteger(jumpInterval);
            updated.hit.enabled = hitEnabled;
            updated.hit.intervalSeconds = parseInteger(hitInterval);
            updated.crouch.enabled = crouchEnabled;
            updated.crouch.intervalSeconds = parseInteger(crouchInterval);
            updated.crouch.durationSeconds = parseInteger(crouchDuration);
            updated.move.enabled = moveEnabled;
            updated.move.intervalSeconds = parseInteger(moveInterval);
            updated.move.durationSeconds = parseInteger(moveDuration);
            updated.movementOrder = parseMovementOrder(movementOrder.getValue());

            ModConfig candidate = draft.copy();
            candidate.antiAfk = updated;
            String error = candidate.validationError();
            if (error != null) {
                status = error;
                return;
            }

            draft.antiAfk = updated;
            minecraft.gui.setScreen(parent);
        } catch (IllegalArgumentException exception) {
            status = exception.getMessage();
        }
    }

    private static int parseInteger(EditBox field) {
        try {
            return Integer.parseInt(field.getValue().trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Enter a whole number in every timing field.");
        }
    }

    private static List<MovementDirection> parseMovementOrder(String value) {
        String[] parts = value.split(",", -1);
        if (parts.length == 0 || parts.length > 16) {
            throw new IllegalArgumentException("Movement order must contain between 1 and 16 directions.");
        }

        List<MovementDirection> order = new ArrayList<>();
        for (String part : parts) {
            String name = part.trim().toUpperCase(Locale.ROOT);
            try {
                order.add(MovementDirection.valueOf(name));
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException("Use FORWARD, LEFT, or RIGHT, separated by commas.");
            }
        }
        return order;
    }

    private static Component toggleMessage(String label, boolean enabled) {
        return Component.literal(label + ": " + (enabled ? "ON" : "OFF"));
    }

    @Override
    public void extractRenderState(
        GuiGraphicsExtractor graphics,
        int mouseX,
        int mouseY,
        float delta
    ) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        int left = Math.max(16, width / 2 - 205);
        graphics.text(font, "Start delay (seconds)", left + 255, 45, 0xFFFFFFFF, true);
        graphics.text(font, "Interval (seconds)", left + 160, 93, 0xFFFFFFFF, true);
        graphics.text(font, "Interval (seconds)", left + 160, 143, 0xFFFFFFFF, true);
        graphics.text(font, "Interval (seconds)", left + 160, 193, 0xFFFFFFFF, true);
        graphics.text(font, "Duration (seconds)", left + 270, 193, 0xFFFFFFFF, true);
        graphics.text(font, "Interval (seconds)", left + 160, 243, 0xFFFFFFFF, true);
        graphics.text(font, "Duration (seconds)", left + 270, 243, 0xFFFFFFFF, true);
        graphics.text(font, "Movement sequence (comma-separated)", left, 298, 0xFFFFFFFF, true);
        if (!status.isEmpty()) {
            graphics.text(font, status, 18, height - 62, 0xFFFF5555, true);
        }
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }
}

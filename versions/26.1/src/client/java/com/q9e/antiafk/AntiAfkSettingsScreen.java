package com.q9e.antiafk;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class AntiAfkSettingsScreen extends Screen {
    private final Screen parent;
    private final ModConfig draft;
    private HeaderAndFooterLayout layout;
    private ScrollableLayout scrollArea;
    private LinearLayout form;
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
        int widgetWidth = Math.max(120, Math.min(360, width - 64));
        int buttonWidth = Math.max(110, Math.min(240, width - 64));

        layout = new HeaderAndFooterLayout(this, 32, 42);
        layout.addTitleHeader(title, font);
        form = LinearLayout.vertical().spacing(5);
        form.defaultCellSetting().alignHorizontallyLeft();

        antiAfkEnabled = settings.enabled;
        jumpEnabled = settings.jump.enabled;
        hitEnabled = settings.hit.enabled;
        crouchEnabled = settings.crouch.enabled;
        moveEnabled = settings.move.enabled;

        addToggle("Anti-AFK", buttonWidth, () -> antiAfkEnabled, value -> antiAfkEnabled = value);
        addLabel("Start delay (seconds)");
        startDelay = numberField(settings.startDelaySeconds, 110, "Start delay");
        addLabel("Jump");
        addToggle("Jump", buttonWidth, () -> jumpEnabled, value -> jumpEnabled = value);
        addLabel("Jump interval (seconds)");
        jumpInterval = numberField(settings.jump.intervalSeconds, 110, "Jump interval");
        addLabel("Hit (one-second left-click hold)");
        addToggle("Hit", buttonWidth, () -> hitEnabled, value -> hitEnabled = value);
        addLabel("Hit interval (seconds)");
        hitInterval = numberField(settings.hit.intervalSeconds, 110, "Hit interval");
        addLabel("Crouch");
        addToggle("Crouch", buttonWidth, () -> crouchEnabled, value -> crouchEnabled = value);
        addLabel("Crouch interval (seconds)");
        crouchInterval = numberField(settings.crouch.intervalSeconds, 110, "Crouch interval");
        addLabel("Crouch hold duration (seconds)");
        crouchDuration = numberField(settings.crouch.durationSeconds, 110, "Crouch duration");
        addLabel("Move");
        addToggle("Move", buttonWidth, () -> moveEnabled, value -> moveEnabled = value);
        addLabel("Move interval (seconds)");
        moveInterval = numberField(settings.move.intervalSeconds, 110, "Move interval");
        addLabel("Move hold duration (seconds)");
        moveDuration = numberField(settings.move.durationSeconds, 110, "Move duration");
        addLabel("Movement sequence, comma-separated");
        movementOrder = textField(
            String.join(", ", settings.movementOrder.stream().map(Enum::name).toList()),
            widgetWidth,
            "Movement sequence"
        );

        scrollArea = new ScrollableLayout(minecraft, form, layout.getContentHeight());
        scrollArea.setMinWidth(Math.min(width - 32, 360));
        layout.addToContents(scrollArea);

        LinearLayout footer = LinearLayout.horizontal().spacing(8);
        footer.addChild(Button.builder(Component.literal("Apply"), button -> apply())
            .bounds(0, 0, 92, 24).build());
        footer.addChild(Button.builder(Component.literal("Cancel"), button -> minecraft.setScreen(parent))
            .bounds(0, 0, 92, 24).build());
        layout.addToFooter(footer);

        repositionElements();
        layout.visitWidgets(this::addRenderableWidget);
    }

    private void addLabel(String text) {
        StringWidget label = new StringWidget(Component.literal(text), font)
            .setMaxWidth(Math.max(120, width - 48));
        form.addChild(label);
    }

    private void addToggle(String label, int buttonWidth, BooleanSupplier read, Consumer<Boolean> write) {
        Button[] reference = new Button[1];
        reference[0] = Button.builder(toggleMessage(label, read.getAsBoolean()), button -> {
            boolean nextValue = !read.getAsBoolean();
            write.accept(nextValue);
            button.setMessage(toggleMessage(label, nextValue));
        }).bounds(0, 0, buttonWidth, 22).build();
        form.addChild(reference[0]);
    }

    private EditBox numberField(int value, int fieldWidth, String narration) {
        return textField(Integer.toString(value), fieldWidth, narration, 8);
    }

    private EditBox textField(String value, int fieldWidth, String narration) {
        return textField(value, fieldWidth, narration, 256);
    }

    private EditBox textField(String value, int fieldWidth, String narration, int maxLength) {
        EditBox field = new EditBox(font, 0, 0, fieldWidth, 20, Component.literal(narration));
        field.setMaxLength(maxLength);
        field.setValue(value);
        form.addChild(field);
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
            minecraft.setScreen(parent);
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
    protected void repositionElements() {
        if (layout == null || scrollArea == null) {
            return;
        }
        form.arrangeElements();
        scrollArea.arrangeElements();
        scrollArea.setMaxHeight(layout.getContentHeight());
        scrollArea.setMinHeight(layout.getContentHeight());
        layout.arrangeElements();
        scrollArea.setX((width - scrollArea.getWidth()) / 2);
        scrollArea.setY(layout.getHeaderHeight());
        restoreFocusedChild();
    }

    private void restoreFocusedChild() {
        if (getFocused() instanceof ContainerEventHandler focusContainer) {
            GuiEventListener focusedChild = focusContainer.getFocused();
            if (focusedChild != null) {
                focusContainer.setFocused(focusedChild);
            }
        }
    }

    @Override
    public void extractRenderState(
        GuiGraphicsExtractor graphics,
        int mouseX,
        int mouseY,
        float delta
    ) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        if (!status.isEmpty()) {
            graphics.text(font, status, 12, height - 56, 0xFFFF5555, true);
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}

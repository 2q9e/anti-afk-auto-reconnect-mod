package com.q9e.antiafk;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ReconnectSettingsScreen extends Screen {
    private final Screen parent;
    private final ModConfig draft;

    private EditBox serverAddress;
    private EditBox firstRetryDelay;
    private EditBox subsequentRetryDelay;
    private final EditBox[] afterReconnectMessages = new EditBox[5];
    private boolean reconnectEnabled;
    private String status = "";

    public ReconnectSettingsScreen(Screen parent, ModConfig draft) {
        super(Component.literal("Auto reconnect settings"));
        this.parent = parent;
        this.draft = draft;
    }

    @Override
    protected void init() {
        ReconnectSettings settings = draft.reconnect;
        int left = Math.max(18, width / 2 - 215);
        reconnectEnabled = settings.enabled;

        addRenderableWidget(toggle(left, 42, 190, "Auto reconnect", () -> reconnectEnabled, value -> reconnectEnabled = value));

        serverAddress = new EditBox(font, left, 87, 410, 20, Component.literal("Server address"));
        serverAddress.setMaxLength(255);
        serverAddress.setValue(settings.serverAddress);
        addRenderableWidget(serverAddress);

        firstRetryDelay = numericField(left, 137, settings.firstRetryDelaySeconds);
        subsequentRetryDelay = numericField(left, 187, settings.subsequentRetryDelaySeconds);

        List<String> messages = draft.afterReconnectMessages == null
            ? List.of("", "", "", "", "")
            : draft.afterReconnectMessages;
        for (int i = 0; i < afterReconnectMessages.length; i++) {
            String value = i < messages.size() && messages.get(i) != null ? messages.get(i) : "";
            EditBox field = new EditBox(font, left, 235 + i * 29, 410, 20, Component.literal("After reconnect message " + (i + 1)));
            field.setMaxLength(256);
            field.setValue(value);
            afterReconnectMessages[i] = field;
            addRenderableWidget(field);
        }

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
        return Button.builder(toggleMessage(label, read.getAsBoolean()), button -> {
            boolean nextValue = !read.getAsBoolean();
            write.accept(nextValue);
            button.setMessage(toggleMessage(label, nextValue));
        }).bounds(x, y, buttonWidth, 22).build();
    }

    private EditBox numericField(int x, int y, int value) {
        EditBox field = new EditBox(font, x, y, 110, 20, Component.empty());
        field.setMaxLength(8);
        field.setValue(Integer.toString(value));
        addRenderableWidget(field);
        return field;
    }

    private void apply() {
        try {
            ReconnectSettings updated = new ReconnectSettings();
            updated.enabled = reconnectEnabled;
            updated.serverAddress = serverAddress.getValue().trim();
            updated.firstRetryDelaySeconds = parseInteger(firstRetryDelay);
            updated.subsequentRetryDelaySeconds = parseInteger(subsequentRetryDelay);

            List<String> messages = new ArrayList<>();
            for (EditBox field : afterReconnectMessages) {
                messages.add(field.getValue());
            }

            ModConfig candidate = draft.copy();
            candidate.reconnect = updated;
            candidate.afterReconnectMessages = messages;
            String error = candidate.validationError();
            if (error != null) {
                status = error;
                return;
            }

            draft.reconnect = updated;
            draft.afterReconnectMessages = messages;
            minecraft.gui.setScreen(parent);
        } catch (IllegalArgumentException exception) {
            status = exception.getMessage();
        }
    }

    private static int parseInteger(EditBox field) {
        try {
            return Integer.parseInt(field.getValue().trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Enter a whole number for each retry delay.");
        }
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
        int left = Math.max(18, width / 2 - 215);
        graphics.text(font, "Server address (host or host:port)", left, 73, 0xFFFFFFFF, true);
        graphics.text(font, "First retry delay (seconds)", left, 123, 0xFFFFFFFF, true);
        graphics.text(font, "Later retry delay (seconds)", left, 173, 0xFFFFFFFF, true);
        graphics.text(font, "Messages or commands after a successful automatic reconnect", left, 218, 0xFFFFFFFF, true);
        if (!status.isEmpty()) {
            graphics.text(font, status, 18, height - 62, 0xFFFF5555, true);
        }
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }
}

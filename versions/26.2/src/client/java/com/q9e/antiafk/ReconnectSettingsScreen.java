package com.q9e.antiafk;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ReconnectSettingsScreen extends Screen {
    private final Screen parent;
    private final ModConfig draft;
    private HeaderAndFooterLayout layout;
    private ScrollableLayout scrollArea;
    private LinearLayout form;
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
        int widgetWidth = Math.max(120, Math.min(360, width - 64));
        int buttonWidth = Math.max(110, Math.min(240, width - 64));
        reconnectEnabled = settings.enabled;

        layout = new HeaderAndFooterLayout(this, 32, 42);
        layout.addTitleHeader(title, font);
        form = LinearLayout.vertical().spacing(5);
        form.defaultCellSetting().alignHorizontallyLeft();

        addToggle("Auto reconnect", buttonWidth, () -> reconnectEnabled, value -> reconnectEnabled = value);
        addLabel("Server address (host or host:port)");
        serverAddress = textField(settings.serverAddress, widgetWidth, "Server address", 255);
        addLabel("First retry delay (seconds)");
        firstRetryDelay = numberField(settings.firstRetryDelaySeconds);
        addLabel("Later retry delay (seconds)");
        subsequentRetryDelay = numberField(settings.subsequentRetryDelaySeconds);
        addLabel("Up to five messages or commands after an automatic reconnect");
        List<String> messages = draft.afterReconnectMessages == null
            ? List.of("", "", "", "", "")
            : draft.afterReconnectMessages;
        for (int i = 0; i < afterReconnectMessages.length; i++) {
            String value = i < messages.size() && messages.get(i) != null ? messages.get(i) : "";
            addLabel("Message " + (i + 1));
            afterReconnectMessages[i] = textField(value, widgetWidth, "After reconnect message " + (i + 1), 256);
        }

        scrollArea = new ScrollableLayout(minecraft, form, layout.getContentHeight());
        scrollArea.setMinWidth(Math.min(width - 32, 360));
        layout.addToContents(scrollArea);

        LinearLayout footer = LinearLayout.horizontal().spacing(8);
        footer.addChild(Button.builder(Component.literal("Apply"), button -> apply())
            .bounds(0, 0, 92, 24).build());
        footer.addChild(Button.builder(Component.literal("Cancel"), button -> minecraft.gui.setScreen(parent))
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
        Button button = Button.builder(toggleMessage(label, read.getAsBoolean()), widget -> {
            boolean nextValue = !read.getAsBoolean();
            write.accept(nextValue);
            widget.setMessage(toggleMessage(label, nextValue));
        }).bounds(0, 0, buttonWidth, 22).build();
        form.addChild(button);
    }

    private EditBox numberField(int value) {
        return textField(Integer.toString(value), 110, "Retry delay", 8);
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
        minecraft.gui.setScreen(parent);
    }
}

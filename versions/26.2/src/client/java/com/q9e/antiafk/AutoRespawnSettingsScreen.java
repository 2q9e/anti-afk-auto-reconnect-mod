package com.q9e.antiafk;

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

public final class AutoRespawnSettingsScreen extends Screen {
    private final Screen parent;
    private final ModConfig draft;
    private HeaderAndFooterLayout layout;
    private ScrollableLayout scrollArea;
    private LinearLayout form;
    private EditBox delay;
    private boolean enabled;
    private String status = "";

    public AutoRespawnSettingsScreen(Screen parent, ModConfig draft) {
        super(Component.literal("Auto Respawn settings"));
        this.parent = parent;
        this.draft = draft;
    }

    @Override
    protected void init() {
        AutoRespawnSettings settings = draft.autoRespawn;
        int buttonWidth = Math.max(110, Math.min(240, width - 64));
        enabled = settings.enabled;

        layout = new HeaderAndFooterLayout(this, 32, 42);
        layout.addTitleHeader(title, font);
        form = LinearLayout.vertical().spacing(6);
        form.defaultCellSetting().alignHorizontallyLeft();

        addToggle("Auto Respawn", buttonWidth, () -> enabled, value -> enabled = value);
        addLabel("Automatically respawn after death-screen delay.");
        addLabel("The delay starts again for each new death.");
        addLabel("Respawn delay (seconds)");
        delay = new EditBox(font, 0, 0, 110, 20, Component.literal("Auto Respawn delay"));
        delay.setMaxLength(8);
        delay.setValue(Integer.toString(settings.delaySeconds));
        form.addChild(delay);

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

    private void apply() {
        AutoRespawnSettings updated = new AutoRespawnSettings();
        updated.enabled = enabled;
        try {
            updated.delaySeconds = Integer.parseInt(delay.getValue().trim());
        } catch (NumberFormatException exception) {
            status = "Enter a whole number for the Auto Respawn delay.";
            return;
        }

        ModConfig candidate = draft.copy();
        candidate.autoRespawn = updated;
        String error = candidate.validationError();
        if (error != null) {
            status = error;

package com.q9e.antiafk;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineTextWidget;

import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class AutoRespawnSettingsScreen extends Screen {
    private final Screen parent;
    private final ModConfig draft;
    private HeaderAndFooterLayout layout;
    private ScrollableForm scrollArea;
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

        scrollArea = new ScrollableForm(form);
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
        MultiLineTextWidget label = new MultiLineTextWidget(Component.literal(text), font)
            .setMaxWidth(Math.max(120, width - 48));
        form.addChild(label);
    }

    private void addToggle(String label, int buttonWidth, BooleanSupplier read, Consumer<Boolean> write) {

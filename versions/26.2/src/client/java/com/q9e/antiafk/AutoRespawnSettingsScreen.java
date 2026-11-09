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

package com.q9e.antiafk;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class SettingsHomeScreen extends Screen {
    private final Screen parent;
    private final ModConfig draft;
    private final AntiAfkClient mod;
    private String status = "";

    public SettingsHomeScreen(Screen parent, ModConfig draft, AntiAfkClient mod) {
        super(Component.literal("Anti-AFK and Auto Reconnect"));
        this.parent = parent;
        this.draft = draft;
        this.mod = mod;
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int buttonWidth = 220;
        int left = centerX - buttonWidth / 2;

        addRenderableWidget(Button.builder(Component.literal("Anti-AFK settings"), button ->
            minecraft.setScreen(new AntiAfkSettingsScreen(this, draft))
        ).bounds(left, height / 2 - 82, buttonWidth, 24).build());

        addRenderableWidget(Button.builder(Component.literal("Auto reconnect settings"), button ->
            minecraft.setScreen(new ReconnectSettingsScreen(this, draft))
        ).bounds(left, height / 2 - 49, buttonWidth, 24).build());

        addRenderableWidget(Button.builder(Component.literal("Auto Welcome settings"), button ->
            minecraft.setScreen(new AutoWelcomeSettingsScreen(this, draft))
        ).bounds(left, height / 2 - 16, buttonWidth, 24).build());

        addRenderableWidget(Button.builder(Component.literal("Save"), button -> save())
            .bounds(left, settingsTop + 145, 105, 24).build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"), button ->
            minecraft.setScreen(parent)
        ).bounds(left + 115, settingsTop + 145, 105, 24).build());
    }

    private void save() {
        String error = mod.applyConfig(Minecraft.getInstance(), draft);
        if (error != null) {
            status = error;
            return;
        }
        minecraft.setScreen(parent);
    }

    @Override
    public void render(
        GuiGraphics graphics,
        int mouseX,
        int mouseY,
        float delta
    ) {
        super.render(graphics, mouseX, mouseY, delta);
        graphics.drawString(font, "Open this screen with the configurable key in Options > Controls.", 18, 24, 0xFFFFFFFF, true);
        if (!status.isEmpty()) {
            graphics.drawString(font, status, 18, height - 10, 0xFFFF5555, true);
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}

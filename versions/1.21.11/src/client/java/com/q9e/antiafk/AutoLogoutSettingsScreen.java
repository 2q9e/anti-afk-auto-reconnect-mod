package com.q9e.antiafk;

import java.util.Arrays;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class AutoLogoutSettingsScreen extends Screen {
    private final Screen parent;
    private final ModConfig draft;
    private HeaderAndFooterLayout layout;
    private ScrollableLayout scrollArea;
    private LinearLayout form;
    private EditBox range;
    private EditBox ignoredUsernames;
    private boolean enabled;
    private String status = "";

    public AutoLogoutSettingsScreen(Screen parent, ModConfig draft) {
        super(Component.literal("Auto Logout settings"));
        this.parent = parent;
        this.draft = draft;
    }

    @Override
    protected void init() {
        AutoLogoutSettings settings = draft.autoLogout;
        int buttonWidth = Math.max(110, Math.min(240, width - 64));
        enabled = settings.enabled;

        layout = new HeaderAndFooterLayout(this, 32, 42);
        layout.addTitleHeader(title, font);
        form = LinearLayout.vertical().spacing(6);
        form.defaultCellSetting().alignHorizontallyLeft();

        addToggle("Auto Logout", buttonWidth, () -> enabled, value -> enabled = value);
        addLabel("Disconnect when another player is nearby.");
        addLabel("Detection radius in blocks (1–15)");
        range = new EditBox(font, 0, 0, 110, 20, Component.literal("Auto Logout range"));
        range.setMaxLength(2);
        range.setValue(Integer.toString(settings.detectionRangeBlocks));
        form.addChild(range);
        addLabel("Ignore usernames (comma-separated, up to 10)");
        ignoredUsernames = new EditBox(font, 0, 0, Math.max(120, Math.min(360, width - 64)), 20,
            Component.literal("Ignored usernames"));
        ignoredUsernames.setMaxLength(190);
        ignoredUsernames.setValue(settings.ignoredUsernames == null
            ? ""
            : String.join(", ", settings.ignoredUsernames));
        form.addChild(ignoredUsernames);

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

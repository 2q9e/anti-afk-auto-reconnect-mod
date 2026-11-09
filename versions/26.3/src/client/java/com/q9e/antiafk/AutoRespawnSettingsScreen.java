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

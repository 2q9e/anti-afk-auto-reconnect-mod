package com.q9e.antiafk;

import java.io.IOException;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public final class AntiAfkClient implements ClientModInitializer {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
        ResourceLocation.fromNamespaceAndPath("anti_afk_reconnect", "main")
    );

    private final AntiAfkScheduler scheduler = new AntiAfkScheduler();
    private final ReconnectController reconnectController = new ReconnectController();
    private ModConfig config;
    private KeyMapping openSettingsKey;

    @Override
    public void onInitializeClient() {
        config = ConfigStore.load();
        openSettingsKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.anti_afk_reconnect.open_settings",
            InputConstants.Type.KEYBOARD,
            InputConstants.KEY_O,
            CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::onEndClientTick);
        ClientPlayConnectionEvents.JOIN.register(
            (listener, sender, client) -> reconnectController.onPlayJoin(client, listener, config)
        );
        ClientPlayConnectionEvents.DISCONNECT.register(
            (listener, client) -> reconnectController.onPlayDisconnect(client, config.reconnect)
        );
    }

    private void onEndClientTick(Minecraft client) {
        while (openSettingsKey.consumeClick()) {
            if (client.gui.screen() == null) {
                client.gui.setScreen(
                    new SettingsHomeScreen(client.gui.screen(), config.copy(), this)
                );
            }
        }

        reconnectController.tick(client, config.reconnect);
        scheduler.tick(client, config.antiAfk);
    }

    public String applyConfig(Minecraft client, ModConfig updated) {
        if (updated == null) {
            return "Configuration is missing.";
        }
        String validationError = updated.validationError();
        if (validationError != null) {
            return validationError;
        }

        try {
            ConfigStore.save(updated);
        } catch (IOException | IllegalArgumentException exception) {
            return "Could not save settings: " + exception.getMessage();
        }

        config = updated.copy();
        scheduler.reset(client);
        reconnectController.onSettingsChanged(config.reconnect);
        return null;
    }
}

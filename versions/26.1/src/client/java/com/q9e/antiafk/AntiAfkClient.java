package com.q9e.antiafk;

import java.io.IOException;
import java.util.Objects;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public final class AntiAfkClient implements ClientModInitializer {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
        Identifier.fromNamespaceAndPath("anti_afk_reconnect", "main")
    );

    private final AntiAfkScheduler scheduler = new AntiAfkScheduler();
    private final ClientChatSendGate chatSendGate = new ClientChatSendGate();
    private final ReconnectController reconnectController = new ReconnectController(chatSendGate);
    private final AutoWelcomeController autoWelcomeController = new AutoWelcomeController(chatSendGate);
    private ModConfig config;
    private KeyMapping openSettingsKey;

    @Override
    public void onInitializeClient() {
        config = ConfigStore.load();
        openSettingsKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.anti_afk_reconnect.open_settings",
            InputConstants.Type.KEYSYM,
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
        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, chatType, receptionTimestamp) -> {
            if (signedMessage == null || sender == null) {
                return;
            }
            Minecraft client = Minecraft.getInstance();
            String signedContent = signedMessage.signedContent();
            client.execute(() -> autoWelcomeController.onChatMessage(
                client,
                sender.id(),
                signedContent,
                config.autoWelcome
            ));
        });
    }

    private void onEndClientTick(Minecraft client) {
        chatSendGate.beginTick();
        while (openSettingsKey.consumeClick()) {
            if (client.screen == null) {
                client.setScreen(
                    new SettingsHomeScreen(client.screen, config.copy(), this)
                );
            }
        }

        autoLogoutController.tick(client, config.autoLogout);
        reconnectController.tick(client, config.reconnect);
        autoWelcomeController.tick(client, config.autoWelcome);
        autoRespawnController.tick(client, config.autoRespawn);
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

        boolean antiAfkChanged = !sameAntiAfk(config.antiAfk, updated.antiAfk);
        boolean reconnectChanged = !sameReconnect(config.reconnect, updated.reconnect);
        boolean autoWelcomeChanged = !sameAutoWelcome(config.autoWelcome, updated.autoWelcome);
        boolean autoRespawnChanged = !sameAutoRespawn(config.autoRespawn, updated.autoRespawn);
        boolean autoLogoutChanged = !sameAutoLogout(config.autoLogout, updated.autoLogout);

        try {
            ConfigStore.save(updated);
        } catch (IOException | IllegalArgumentException exception) {
            return "Could not save settings: " + exception.getMessage();
        }

        config = updated.copy();
        if (antiAfkChanged) {
            scheduler.reset(client);
        }
        if (reconnectChanged) {
            reconnectController.onSettingsChanged(config.reconnect);
        }
        if (autoWelcomeChanged) {
            autoWelcomeController.onSettingsChanged();
        }
        if (autoRespawnChanged) {
            autoRespawnController.onSettingsChanged();
        }
        if (autoLogoutChanged) {
            autoLogoutController.onSettingsChanged();
        }
        return null;
    }

    private static boolean sameAntiAfk(AntiAfkSettings left, AntiAfkSettings right) {
        return left.enabled == right.enabled
            && left.startDelaySeconds == right.startDelaySeconds
            && sameTap(left.jump, right.jump)
            && sameTap(left.hit, right.hit)
            && sameTimed(left.crouch, right.crouch)
            && sameTimed(left.move, right.move)
            && Objects.equals(left.movementOrder, right.movementOrder);
    }

    private static boolean sameTap(TapActionSettings left, TapActionSettings right) {
        return left.enabled == right.enabled
            && left.intervalSeconds == right.intervalSeconds;
    }

    private static boolean sameTimed(TimedActionSettings left, TimedActionSettings right) {
        return left.enabled == right.enabled
            && left.intervalSeconds == right.intervalSeconds
            && left.durationSeconds == right.durationSeconds;
    }

    private static boolean sameAutoWelcome(AutoWelcomeSettings left, AutoWelcomeSettings right) {
        return left.enabled == right.enabled && left.delaySeconds == right.delaySeconds;
    }

    private static boolean sameAutoRespawn(AutoRespawnSettings left, AutoRespawnSettings right) {
        return left.enabled == right.enabled && left.delaySeconds == right.delaySeconds;
    }

    private static boolean sameAutoLogout(AutoLogoutSettings left, AutoLogoutSettings right) {
        return left.enabled == right.enabled
            && left.detectionRangeBlocks == right.detectionRangeBlocks
            && Objects.equals(left.ignoredUsernames, right.ignoredUsernames);
    }

    private static boolean sameReconnect(ReconnectSettings left, ReconnectSettings right) {
        return left.enabled == right.enabled
            && Objects.equals(left.serverAddress, right.serverAddress)
            && left.firstRetryDelaySeconds == right.firstRetryDelaySeconds
            && left.subsequentRetryDelaySeconds == right.subsequentRetryDelaySeconds;
    }
}

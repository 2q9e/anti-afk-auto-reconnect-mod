package com.q9e.antiafk;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConfigStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = LoggerFactory.getLogger("anti_afk_reconnect");

    private ConfigStore() {
    }
    public static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("anti-afk-auto-reconnect.json");
    }

    public static ModConfig load() {
        Path path = configPath();
        if (!Files.exists(path)) {
            return new ModConfig();
        }

        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            ModConfig config = GSON.fromJson(reader, ModConfig.class);
            if (config == null || config.validationError() != null) {
                LOGGER.warn("Configuration is invalid; using defaults: {}", config == null ? "empty file" : config.validationError());
                return new ModConfig();
            }
            return config;
        } catch (IOException | JsonParseException exception) {
            LOGGER.warn("Could not read configuration at {}; using defaults.", path, exception);
            return new ModConfig();
        }
    }

    public static void save(ModConfig config) throws IOException {
        String error = config.validationError();
        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        Path path = configPath();
        Files.createDirectories(path.getParent());
        Path temporaryPath = path.resolveSibling(path.getFileName() + ".tmp");
        Files.writeString(temporaryPath, GSON.toJson(config), StandardCharsets.UTF_8);

        try {
            Files.move(temporaryPath, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(temporaryPath, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }

}

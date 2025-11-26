package me.kmathers.sudobot.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    private static final String TOKEN_FILE = "token.json";
    private static final Gson gson = new Gson();
    private static JsonObject config;

    static {
        loadConfig();
    }

    private static void loadConfig() {
        try {
            String content = new String(Files.readAllBytes(Paths.get(TOKEN_FILE)));
            config = gson.fromJson(content, JsonObject.class);
            logger.info("Configuration loaded successfully");
        } catch (IOException e) {
            logger.error("Failed to load configuration from {}", TOKEN_FILE, e);
            config = new JsonObject();
        }
    }

    public static void saveConfig() {
        try (FileWriter writer = new FileWriter(TOKEN_FILE)) {
            gson.toJson(config, writer);
            logger.info("Configuration saved successfully");
        } catch (IOException e) {
            logger.error("Failed to save configuration", e);
        }
    }

    public static String getDiscordToken() {
        return config.has("TOKEN") ? config.get("TOKEN").getAsString() : null;
    }

    public static String getGithubToken() {
        return config.has("GITHUB_TOKEN") ? config.get("GITHUB_TOKEN").getAsString() : null;
    }

    public static String getWakatimeToken() {
        return config.has("WAKATIME_TOKEN") ? config.get("WAKATIME_TOKEN").getAsString() : null;
    }
}
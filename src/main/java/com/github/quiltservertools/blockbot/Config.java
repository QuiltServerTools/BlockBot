package com.github.quiltservertools.blockbot;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class Config {
    private static final Path path = FabricLoader.getInstance().getConfigDir().resolve("blockbot.json");
    private String identifier;
    private String channel;
    private String webhook;
    private String adminRoleId;

    public Config() {
        JsonObject json;
        try {
            json = new JsonParser().parse(new String(Files.readAllBytes(path))).getAsJsonObject();
            identifier = json.get("token").getAsString();
            channel = json.get("channel_id").getAsString();
            webhook = json.get("webhook").getAsString();
            adminRoleId = json.get("op_role_id").getAsString();
        } catch (IOException e) {
            // Create default
            try {
                Files.copy(Objects.requireNonNull(Config.class.getResourceAsStream("/data/blockbot/files/default_config.json")), path);
                json = new JsonParser().parse(new String(Files.readAllBytes(path))).getAsJsonObject();
                identifier = json.get("token").getAsString();
                channel = json.get("channel_id").getAsString();
                webhook = json.get("webhook").getAsString();
                adminRoleId = json.get("op_role_id").getAsString();
            } catch (IOException ioException) {
                ioException.printStackTrace();
                BlockBot.LOG.error("Unable to create default config");
                BlockBot.LOG.error("Please fill out the config file for BlockBot");
            }
        }
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public String getChannel() {
        return this.channel;
    }

    public String getAdminRoleId() {
        return adminRoleId;
    }

    public void shutdown() {
        JsonObject o = new JsonObject();
        o.addProperty("token", this.identifier);
        o.addProperty("channel_id", this.channel);
        o.addProperty("webhook", this.webhook);
        o.addProperty("op_role_id", this.adminRoleId);
        try {
            Files.write(path, new GsonBuilder().setPrettyPrinting().create().toJson(o).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            BlockBot.LOG.error("Unable to save BlockBot config");
        }
    }

    public String getWebhook() {
        return this.webhook;
    }
}

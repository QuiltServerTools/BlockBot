package com.github.quiltservertools.blockbot;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.entities.Role;
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
    private boolean inlineCommands;
    private String logo;
    private String name;
    private boolean deathMessages;
    private boolean statusMessages;
    private boolean advancementMessages;

    public Config() {
        JsonObject json;
        try {
            json = new JsonParser().parse(new String(Files.readAllBytes(path))).getAsJsonObject();
            loadFromJson(json);
        } catch (IOException e) {
            // Create default
            try {
                Files.copy(Objects.requireNonNull(Config.class.getResourceAsStream("/data/blockbot/files/default_config.json")), path);
                json = new JsonParser().parse(new String(Files.readAllBytes(path))).getAsJsonObject();
                loadFromJson(json);
                BlockBot.LOG.fatal("Unable to load config file for BlockBot");
                BlockBot.LOG.fatal("Please fill out the config file for BlockBot, found in config/blockbot.json");
            } catch (IOException ioException) {
                ioException.printStackTrace();
                BlockBot.LOG.fatal("Unable to create default config");
            }
        }
    }

    private void loadFromJson(JsonObject json) {
        identifier = json.get("token").getAsString();
        channel = json.get("channel_id").getAsString();
        webhook = json.get("webhook").getAsString();
        adminRoleId = json.get("op_role_id").getAsString();
        inlineCommands = json.get("inline_commands").getAsBoolean();
        name = json.get("name").getAsString();
        logo = json.get("logo_url").getAsString();
        deathMessages = json.get("death_messages").getAsBoolean();
        statusMessages = json.get("status_messages") == null || json.get("status_messages").getAsBoolean();
        advancementMessages = json.get("advancement_messages") == null || json.get("advancement_messages").getAsBoolean();
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public String getChannel() {
        return this.channel;
    }

    public String getLogo() {
        return logo;
    }

    public String getName() {
        return name;
    }

    public String getAdminRoleId() {
        return adminRoleId;
    }

    public boolean adminRole(Role role) {
        return role.getId().equals(this.adminRoleId);
    }

    public boolean enableInlineCommands() {
        return inlineCommands;
    }

    public boolean enableDeathMessages() {
        return deathMessages;
    }

    public boolean sendStatusMessages() {
        return statusMessages;
    }

    public boolean sendAdvancementMessages() {
        return advancementMessages;
    }

    public void shutdown() {
        JsonObject o = new JsonObject();
        o.addProperty("token", this.identifier);
        o.addProperty("channel_id", this.channel);
        o.addProperty("webhook", this.webhook);
        o.addProperty("op_role_id", this.adminRoleId);
        o.addProperty("name", this.name);
        o.addProperty("logo_url", this.logo);
        o.addProperty("inline_commands", this.inlineCommands);
        o.addProperty("death_messages", this.deathMessages);
        o.addProperty("status_messages", this.statusMessages);
        o.addProperty("advancement_messages", this.advancementMessages);
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

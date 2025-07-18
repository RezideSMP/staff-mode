package rezide.staffmode;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StaffModeConfig {
    // Default values
    private String discordBotToken = "YOUR_DISCORD_BOT_TOKEN_HERE"; // IMPORTANT: Replace this
    private long adminLogChannelId = 0L; // Default to 0, indicating it needs to be set
    private long serverStatusChannelId = 0L; // New: Default to 0 for server status channel
    private int discordBotHttpPort = 8080;

    // New fields for file logging
    private boolean logToFileEnabled = true; // Default to false
    private String logFilePath = "logs/staff-mode-server.log"; // Default log file path

    // --- Getters for your configuration values ---
    public String getDiscordBotToken() {
        return discordBotToken;
    }

    public long getAdminLogChannelId() {
        return adminLogChannelId;
    }

    public long getServerStatusChannelId() {
        return serverStatusChannelId;
    }

    public int getDiscordBotHttpPort() {
        return discordBotHttpPort;
    }

    // New getters for file logging
    public boolean isLogToFileEnabled() {
        return logToFileEnabled;
    }

    public String getLogFilePath() {
        return logFilePath;
    }

    // --- Static methods for loading/saving config ---
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = Paths.get("config", StaffMode.MOD_ID + ".json");
    private static StaffModeConfig INSTANCE;

    public static StaffModeConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    private static StaffModeConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                StaffModeConfig config = GSON.fromJson(reader, StaffModeConfig.class);
                StaffMode.LOGGER.info("Loaded config from {}", CONFIG_PATH);
                // If a new config value is added later, ensure it's not null and save
                boolean changed = false;

                // Existing checks
                if (config.discordBotToken == null || config.discordBotToken.isEmpty() || config.discordBotToken.equals("YOUR_DISCORD_BOT_TOKEN_HERE")) {
                    StaffMode.LOGGER.warn("Discord Bot Token is not set in config. Please update config/staff-mode.json");
                    config.discordBotToken = "YOUR_DISCORD_BOT_TOKEN_HERE"; // Ensure default is there if user deleted it
                    changed = true;
                }
                if (config.adminLogChannelId == 0L) {
                    StaffMode.LOGGER.warn("Discord Admin Log Channel ID is not set in config. Please update config/staff-mode.json");
                    changed = true;
                }
                if (config.serverStatusChannelId == 0L) {
                    StaffMode.LOGGER.warn("Discord Server Status Channel ID is not set in config. Please update config/staff-mode.json");
                    changed = true;
                }
                if (config.discordBotHttpPort == 0) { // Should not be 0, provide a default if somehow corrupted
                    StaffMode.LOGGER.warn("Discord Bot HTTP Port is not set in config. Using default 8080.");
                    config.discordBotHttpPort = 8080;
                    changed = true;
                }

                // New checks for file logging properties
                // Gson will set boolean primitives to their default (false) if missing, which is fine.
                // For String, it will be null if missing, so we explicitly check and set defaults.
                if (config.logFilePath == null || config.logFilePath.isEmpty()) {
                    StaffMode.LOGGER.warn("Log file path is not set in config. Using default 'logs/staff-mode-server.log'.");
                    config.logFilePath = "logs/staff-mode-server.log";
                    changed = true;
                }
                // If logToFileEnabled is false, and it was true previously in the file but removed, it would
                // default to false. No explicit check for boolean needed unless you want to force a default.
                // The default `false` for `logToFileEnabled` means it will only be true if explicitly set in the config.

                if (changed) {
                    save(config); // Save with any defaults applied
                }
                return config;
            } catch (Exception e) {
                StaffMode.LOGGER.error("Error loading config from {}. Creating new config. Error: {}", CONFIG_PATH, e.getMessage());
                // Fallback to new instance if loading fails
                StaffModeConfig newConfig = new StaffModeConfig();
                save(newConfig); // Save the default config
                return newConfig;
            }
        } else {
            StaffMode.LOGGER.info("Config file not found at {}. Creating default config.", CONFIG_PATH);
            StaffModeConfig newConfig = new StaffModeConfig();
            save(newConfig); // Save the default config
            return newConfig;
        }
    }

    public static void save(StaffModeConfig config) {
        try {
            Files.createDirectories(CONFIG_PATH.getParent()); // Ensure config directory exists
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(config, writer);
            }
            StaffMode.LOGGER.info("Saved config to {}", CONFIG_PATH);
        } catch (Exception e) {
            StaffMode.LOGGER.error("Error saving config to {}: {}", CONFIG_PATH, e.getMessage());
        }
    }
}
package dev.punjarrr.gainExpDisplay.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigUpdater {

    private static final String CURRENT_CONFIG_VERSION = "3.1.0";

    // Default 3.0.0 messages to fall back on if a key isn't found in legacy config
    private static final String DEFAULT_GAIN_XP       = "<green><bold>Gain XP:</bold> <yellow><bold>+%amount%</bold>";
    private static final String DEFAULT_MENDING_REPAIR = "<aqua><bold>Mending Repair:</bold> <yellow><bold>+%amount%</bold> <green>(%durability%/%max%)";
    private static final String DEFAULT_RELOAD         = "<green>Configuration reloaded successfully.";
    private static final String DEFAULT_NO_PERM        = "<red>You do not have permission to use this command.";
    private static final String DEFAULT_CMD_ERROR      = "<yellow>Usage: /ged reload";

    private final File dataFolder;
    private final Logger log;

    public ConfigUpdater(File dataFolder, Logger log) {
        this.dataFolder = dataFolder;
        this.log = log;
    }

    // -------------------------------------------------------------------------
    // Entry point
    // -------------------------------------------------------------------------

    /**
     * Call this before loading config normally.
     * Detects the installed version and migrates to 3.0.0 if needed.
     */
    public void migrateIfNeeded() {
        File configFile = new File(dataFolder, "config.yml");

        if (!configFile.exists()) return;

        FileConfiguration existing = YamlConfiguration.loadConfiguration(configFile);
        String installedVersion = existing.getString("config-version", null);

        if (CURRENT_CONFIG_VERSION.equals(installedVersion)) return;

        if ("3.0.0".equals(installedVersion)) {
            // 2.2.0 and older: add per-player-language, switched off so every player keeps the same language
            log.info("Updating config.yml to " + CURRENT_CONFIG_VERSION + ": added per-player-language (off).");
            try {
                migrate(existing, LegacyVersion.V3_0_0, configFile);
            } catch (IOException e) {
                log.log(Level.SEVERE, "Config update failed: " + e.getMessage(), e);
            }
            return;
        }

        if (installedVersion != null) {
            if (isNewerThan(installedVersion, CURRENT_CONFIG_VERSION)) {
                log.warning("Config version '" + installedVersion + "' is newer than expected. Skipping migration.");
            }
            return;
        }

        // No config-version → legacy. Fingerprint which one.
        LegacyVersion detected = detectLegacyVersion(existing);
        log.info("Detected legacy config version: " + detected + ". Migrating to " + CURRENT_CONFIG_VERSION + "…");

        try {
            migrate(existing, detected, configFile);
            log.info("Migration complete! Please review your config.yml and translations/en_US.yml.");
        } catch (IOException e) {
            log.log(Level.SEVERE, "Migration failed: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Detection
    // -------------------------------------------------------------------------

    private enum LegacyVersion { V1_0_0, V2_0_0, V2_1_0, V_VOID, V3_0_0 }

    private LegacyVersion detectLegacyVersion(FileConfiguration cfg) {
        // Void version: same structure as 3.0.0 but no config-version
        // Detect by checking if translations/en_US.yml already exists
        File enUs = new File(dataFolder, "translations/en_US.yml");
        if (enUs.exists()) {
            return LegacyVersion.V_VOID;
        }

        boolean hasMessagesSection   = cfg.isConfigurationSection("messages");
        boolean hasFlatGainXpMessage = cfg.isString("messages.gain-xp");
        boolean hasNestedGainXp      = cfg.isConfigurationSection("gain-xp");

        if (hasFlatGainXpMessage && !hasNestedGainXp) return LegacyVersion.V1_0_0;
        if (hasNestedGainXp && hasMessagesSection)    return LegacyVersion.V2_1_0;
        if (hasNestedGainXp)                          return LegacyVersion.V2_0_0;

        log.warning("Could not confidently detect legacy version. Treating as 1.0.0.");
        return LegacyVersion.V1_0_0;
    }

    // -------------------------------------------------------------------------
    // Migration
    // -------------------------------------------------------------------------

    private void migrate(FileConfiguration legacy, LegacyVersion version, File configFile) throws IOException {
        backup(configFile);

        MigratedValues values = extract(legacy, version);
        writeNewConfig(configFile, values);

        // Void and 3.0.0 configs already have translation files; don't overwrite them
        if (version != LegacyVersion.V_VOID && version != LegacyVersion.V3_0_0) {
            File translationsDir = new File(dataFolder, "translations");
            translationsDir.mkdirs();
            writeEnUsTranslation(new File(translationsDir, "en_US.yml"), values);
        }
    }

    // -------------------------------------------------------------------------
    // Value extraction
    // -------------------------------------------------------------------------

    /** Holds every user-configured value we want to preserve across versions. */
    static class MigratedValues {
        boolean perPlayerLanguage    = false; // existing servers keep one language until they turn it on
        boolean gainXpEnabled        = true;
        boolean mendingRepairEnabled = true;
        String lang          = "en_US"; // preserve user's language setting
        boolean requirePermission = false;

        // Messages (only relevant for pre-void versions)
        String gainXp        = DEFAULT_GAIN_XP;
        String mendingRepair = DEFAULT_MENDING_REPAIR;
        String reload        = DEFAULT_RELOAD;
        String noPermission  = DEFAULT_NO_PERM;
        String cmdError      = DEFAULT_CMD_ERROR;
    }

    private MigratedValues extract(FileConfiguration cfg, LegacyVersion version) {
        MigratedValues v = new MigratedValues();

        v.lang              = cfg.getString("lang",               "en_US");
        v.requirePermission = cfg.getBoolean("require-permission", false);

        switch (version) {

            case V1_0_0: {
                // messages.gain-xp  (flat string)
                // messages.mending-repair (flat string)
                // No enabled flags existed
                v.gainXp        = cfg.getString("messages.gain-xp",        DEFAULT_GAIN_XP);
                v.mendingRepair = cfg.getString("messages.mending-repair",  DEFAULT_MENDING_REPAIR);
                break;
            }

            case V2_0_0: {
                // gain-xp.enabled / gain-xp.message
                // mending-repair.enabled / mending-repair.message
                // No messages.* section
                v.gainXpEnabled        = cfg.getBoolean("gain-xp.enabled",        true);
                v.mendingRepairEnabled = cfg.getBoolean("mending-repair.enabled",  true);
                v.gainXp               = cfg.getString("gain-xp.message",          DEFAULT_GAIN_XP);
                v.mendingRepair        = cfg.getString("mending-repair.message",   DEFAULT_MENDING_REPAIR);
                break;
            }

            case V2_1_0: {
                // Same as 2.0.0 but also has messages.reload / messages.command-*
                v.gainXpEnabled        = cfg.getBoolean("gain-xp.enabled",              true);
                v.mendingRepairEnabled = cfg.getBoolean("mending-repair.enabled",        true);
                v.gainXp               = cfg.getString("gain-xp.message",               DEFAULT_GAIN_XP);
                v.mendingRepair        = cfg.getString("mending-repair.message",         DEFAULT_MENDING_REPAIR);
                v.reload               = cfg.getString("messages.reload",               DEFAULT_RELOAD);
                v.noPermission         = cfg.getString("messages.command-no-permission", DEFAULT_NO_PERM);
                v.cmdError             = cfg.getString("messages.command-error",         DEFAULT_CMD_ERROR);
                break;
            }

            case V_VOID:
            case V3_0_0: {
                // Same structure as 3.0.0 — just read values directly
                v.gainXpEnabled        = cfg.getBoolean("gain-xp.enabled",       true);
                v.mendingRepairEnabled = cfg.getBoolean("mending-repair.enabled", true);
                // lang is preserved separately in MigratedValues (see below)
                break;
            }
        }

        return v;
    }

    // -------------------------------------------------------------------------
    // Writers
    // -------------------------------------------------------------------------

    private void writeNewConfig(File configFile, MigratedValues v) throws IOException {
        Files.writeString(configFile.toPath(), render(v), StandardOpenOption.TRUNCATE_EXISTING);
    }

    /** config.yml with these values. With the defaults and per-player-language on, it equals the bundled config.yml. */
    static String render(MigratedValues v) {
        return
                "# Configuration version, DO NOT MODIFY THIS VALUE!\n" +
                        "config-version: " + CURRENT_CONFIG_VERSION + "\n" +
                        "\n" +
                        "######################################################\n" +
                        "#                     Language                       #\n" +
                        "######################################################\n" +
                        "\n" +
                        "# Language setting (available: en_US, es_ES, de_DE, fr_FR, zh_CN, ja_JP, th_TH)\n" +
                        "# If the specified language file is missing, it will fall back to en_US.\n" +
                        "lang: " + v.lang + "\n" +
                        "\n" +
                        "# Adding custom language\n" +
                        "# 1. Create a new file in the translations directory\n" +
                        "# 2. Use en_US.yml as a template\n" +
                        "# 3. Modify the file as needed\n" +
                        "# 4. Set language to your custom language file name\n" +
                        "\n" +
                        "# When set to true, each player sees the messages in their own Minecraft language\n" +
                        "# (for example th_TH when their game is set to Thai), if there is a translation\n" +
                        "# file for it. Other players, and the console, use the language set above.\n" +
                        "per-player-language: " + v.perPlayerLanguage + "\n" +
                        "\n" +
                        "######################################################\n" +
                        "#                    Permissions                     #\n" +
                        "######################################################\n" +
                        "\n" +
                        "# When set to true, players require permissions to see any message.\n" +
                        "# This setting applies globally to all message types.\n" +
                        "# See below for a list of permissions.\n" +
                        "require-permission: " + v.requirePermission + "\n" +
                        "\n" +
                        "# Permissions for displaying messages:\n" +
                        "#\n" +
                        "#  gainexpdisplay.gainexp - Display text when gaining experience\n" +
                        "#  gainexpdisplay.mending - Display text when repairing items with Mending\n" +
                        "\n" +
                        "######################################################\n" +
                        "#                      Settings                      #\n" +
                        "######################################################\n" +
                        "\n" +
                        "# When set to true, players will see text when gaining exp.\n" +
                        "gain-xp:\n" +
                        "  enabled: " + v.gainXpEnabled + "\n" +
                        "\n" +
                        "# When set to true, players will see text when repairing items with Mending.\n" +
                        "mending-repair:\n" +
                        "  enabled: " + v.mendingRepairEnabled + "\n";
    }

    private void writeEnUsTranslation(File file, MigratedValues v) throws IOException {
        String content =
                "# ==================================================\n" +
                        "# Placeholders\n" +
                        "# %amount%      - Amount of XP gained\n" +
                        "# %durability%  - Current durability of the repaired item\n" +
                        "# %max%         - Maximum durability of the repaired item\n" +
                        "# ==================================================\n" +
                        "\n" +
                        "# Message displayed when the player gains experience\n" +
                        "gain-xp: \"" + escapeYaml(v.gainXp) + "\"\n" +
                        "\n" +
                        "# Message displayed when an item is repaired by Mending\n" +
                        "mending-repair: \"" + escapeYaml(v.mendingRepair) + "\"\n" +
                        "\n" +
                        "# Command feedback messages\n" +
                        "reload: \"" + escapeYaml(v.reload) + "\"\n" +
                        "command-no-permission: \"" + escapeYaml(v.noPermission) + "\"\n" +
                        "command-error: \"" + escapeYaml(v.cmdError) + "\"\n";

        Files.writeString(file.toPath(), content,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Creates a timestamped backup of the given file alongside it. */
    private void backup(File original) throws IOException {
        File backupDir = new File(dataFolder, "backup");
        backupDir.mkdirs();
        String timestamp = String.valueOf(System.currentTimeMillis());
        File backup = new File(backupDir, "config.yml.backup-" + timestamp);
        Files.copy(original.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
        log.info("Backed up old config to: backup/" + backup.getName());
    }

    /**
     * Escapes double-quotes inside a string so it's safe to embed in
     * YAML double-quoted scalars.
     */
    private String escapeYaml(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private boolean isNewerThan(String a, String b) {
        String[] partsA = a.split("\\.");
        String[] partsB = b.split("\\.");
        int len = Math.max(partsA.length, partsB.length);
        for (int i = 0; i < len; i++) {
            int numA = i < partsA.length ? Integer.parseInt(partsA[i]) : 0;
            int numB = i < partsB.length ? Integer.parseInt(partsB[i]) : 0;
            if (numA != numB) return numA > numB;
        }
        return false;
    }

}

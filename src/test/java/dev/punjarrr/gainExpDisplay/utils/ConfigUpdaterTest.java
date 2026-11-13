package dev.punjarrr.gainExpDisplay.utils;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigUpdaterTest {

    private static final Logger LOG = Logger.getLogger("test");

    @Test
    void migrates2_1_0ConfigAndKeepsTheUsersSettingsAndMessages(@TempDir Path dir) throws Exception {
        Files.writeString(dir.resolve("config.yml"), String.join("\n",
                "lang: th_TH",
                "require-permission: true",
                "gain-xp:",
                "  enabled: false",
                "  message: \"<red>+%amount% xp\"",
                "mending-repair:",
                "  enabled: true",
                "  message: \"<blue>fixed\"",
                "messages:",
                "  reload: \"<green>done\"",
                ""));

        new ConfigUpdater(dir.toFile(), LOG).migrateIfNeeded();

        YamlConfiguration config = YamlConfiguration.loadConfiguration(dir.resolve("config.yml").toFile());
        assertEquals("3.1.0", config.getString("config-version"));
        assertFalse(config.getBoolean("per-player-language"), "existing servers keep one language");
        assertEquals("th_TH", config.getString("lang"));
        assertTrue(config.getBoolean("require-permission"));
        assertFalse(config.getBoolean("gain-xp.enabled"));

        YamlConfiguration en = YamlConfiguration.loadConfiguration(dir.resolve("translations/en_US.yml").toFile());
        assertEquals("<red>+%amount% xp", en.getString("gain-xp"));
        assertEquals("<blue>fixed", en.getString("mending-repair"));
        assertEquals("<green>done", en.getString("reload"));

        File[] backups = dir.resolve("backup").toFile().listFiles();
        assertEquals(1, backups == null ? 0 : backups.length, "the old config is backed up");
    }

    @Test
    void migrates1_0_0Messages(@TempDir Path dir) throws Exception {
        Files.writeString(dir.resolve("config.yml"), "messages:\n  gain-xp: \"<gold>xp %amount%\"\n");

        new ConfigUpdater(dir.toFile(), LOG).migrateIfNeeded();

        YamlConfiguration en = YamlConfiguration.loadConfiguration(dir.resolve("translations/en_US.yml").toFile());
        assertEquals("<gold>xp %amount%", en.getString("gain-xp"));
    }

    @Test
    void leavesACurrentConfigAlone(@TempDir Path dir) throws Exception {
        String current = "config-version: 3.1.0\nlang: de_DE\n";
        Files.writeString(dir.resolve("config.yml"), current);

        new ConfigUpdater(dir.toFile(), LOG).migrateIfNeeded();

        assertEquals(current, Files.readString(dir.resolve("config.yml")));
        assertFalse(dir.resolve("backup").toFile().exists());
    }

    @Test
    void adds_per_player_language_switchedOff_to_a_2_2_0_config(@TempDir Path dir) throws Exception {
        Files.createDirectories(dir.resolve("translations"));
        String customEnglish = "gain-xp: \"<red>my own text\"\n";
        Files.writeString(dir.resolve("translations/en_US.yml"), customEnglish);
        Files.writeString(dir.resolve("config.yml"), String.join("\n",
                "config-version: 3.0.0",
                "lang: fr_FR",
                "require-permission: true",
                "gain-xp:",
                "  enabled: true",
                "mending-repair:",
                "  enabled: false",
                ""));

        new ConfigUpdater(dir.toFile(), LOG).migrateIfNeeded();

        YamlConfiguration config = YamlConfiguration.loadConfiguration(dir.resolve("config.yml").toFile());
        assertEquals("3.1.0", config.getString("config-version"));
        assertFalse(config.getBoolean("per-player-language"));
        assertEquals("fr_FR", config.getString("lang"));
        assertTrue(config.getBoolean("require-permission"));
        assertFalse(config.getBoolean("mending-repair.enabled"));
        assertEquals(customEnglish, Files.readString(dir.resolve("translations/en_US.yml")), "translations are kept");
    }

    @Test
    void theUpdatersConfigMatchesTheBundledConfig() throws Exception {
        ConfigUpdater.MigratedValues defaults = new ConfigUpdater.MigratedValues();
        defaults.perPlayerLanguage = true; // new installs
        try (var in = getClass().getResourceAsStream("/config.yml")) {
            assertEquals(new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8), ConfigUpdater.render(defaults));
        }
    }
}

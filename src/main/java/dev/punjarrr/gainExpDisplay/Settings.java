package dev.punjarrr.gainExpDisplay;

import org.bukkit.configuration.ConfigurationSection;

/**
 * The values from config.yml, read once on startup and on /ged reload. Kept as one unchangeable object so
 * events on other threads (Folia) always see a complete, consistent set.
 */
public record Settings(String lang, boolean requirePermission, boolean gainXp, boolean mendingRepair) {

    public static Settings from(ConfigurationSection config) {
        return new Settings(
                config.getString("lang", "en_US"),
                config.getBoolean("require-permission", false),
                config.getBoolean("gain-xp.enabled", true),
                config.getBoolean("mending-repair.enabled", true));
    }
}

package dev.punjarrr.gainExpDisplay.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class LanguageLoader {

    private final JavaPlugin plugin;
    private FileConfiguration lang;
    private String currentLang;

    public LanguageLoader(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load(String langName) {
        File folder = new File(plugin.getDataFolder(), "translations");
        if (!folder.exists()) folder.mkdirs();

        File langFile = new File(folder, langName + ".yml");

        if (!langFile.exists()) {
            plugin.getLogger().warning("Language " + langName + " not found. Falling back to en_US.");

            langName = "en_US";
            langFile = new File(folder, "en_US.yml");

            if (!langFile.exists()) {
                plugin.saveResource("translations/en_US.yml", false);
            }
        }

        this.currentLang = langName;
        this.lang = YamlConfiguration.loadConfiguration(langFile);
    }

    public String get(String key) {
        if (lang == null) return "Language not loaded";

        String msg = lang.getString(key);

        if (msg == null) {
            return "Missing message: " + key;
        }

        return msg;
    }

    public String getCurrentLang() {
        return currentLang;
    }
}

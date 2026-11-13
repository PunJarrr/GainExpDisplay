package dev.punjarrr.gainExpDisplay.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class LanguageLoader {

    private static final String[] BUNDLED = {"en_US", "es_ES", "de_DE", "fr_FR", "zh_CN", "ja_JP", "th_TH"};

    private final JavaPlugin plugin;
    // Every translation file (name without .yml -> contents). Replaced as a whole on reload; volatile so
    // events on other threads (Folia) always see a complete set.
    private volatile Map<String, FileConfiguration> languages = Map.of();
    private volatile String defaultLang = "en_US";

    public LanguageLoader(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /** Loads every file in translations/, and uses {@code langName} for the console and as the fallback. */
    public void load(String langName) {
        File folder = new File(plugin.getDataFolder(), "translations");
        if (!folder.exists()) folder.mkdirs();

        copyBundledTranslations(folder);

        Map<String, FileConfiguration> loaded = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                String name = file.getName().substring(0, file.getName().length() - ".yml".length());
                loaded.put(name, YamlConfiguration.loadConfiguration(file));
            }
        }

        String chosen = loaded.keySet().stream().filter(langName::equalsIgnoreCase).findFirst().orElse(null);
        if (chosen == null) {
            plugin.getLogger().warning("Language " + langName + " not found. Falling back to en_US.");
            chosen = "en_US";
        }

        this.languages = Collections.unmodifiableMap(loaded);
        this.defaultLang = chosen;
    }

    private void copyBundledTranslations(File translationsDir) {
        // en_US is included so a fresh install doesn't warn "Language en_US not found"
        for (String locale : BUNDLED) {
            File dest = new File(translationsDir, locale + ".yml");
            if (!dest.exists()) {
                plugin.saveResource("translations/" + locale + ".yml", false);
            }
        }
    }

    /** A message in the server's configured language. */
    public String get(String key) {
        return get(key, null);
    }

    /**
     * A message in the translation that best fits {@code clientLocale} (a player's Minecraft language), or in
     * the configured language when {@code clientLocale} is null or nothing fits. A message missing from the
     * player's file comes from the configured language instead.
     */
    public String get(String key, String clientLocale) {
        Map<String, FileConfiguration> current = languages;
        String fallback = defaultLang;
        String name = clientLocale == null ? fallback : LanguageMatcher.match(clientLocale, current.keySet(), fallback);

        String msg = lookup(current, name, key);
        if (msg == null && !name.equals(fallback)) msg = lookup(current, fallback, key);
        return msg != null ? msg : "Missing message: " + key;
    }

    private static String lookup(Map<String, FileConfiguration> languages, String name, String key) {
        FileConfiguration file = languages.get(name);
        return file == null ? null : file.getString(key);
    }

    public String getCurrentLang() {
        return defaultLang;
    }
}

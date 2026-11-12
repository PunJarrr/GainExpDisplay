package dev.punjarrr.gainExpDisplay;

import dev.punjarrr.gainExpDisplay.commands.ged.GedCommand;
import dev.punjarrr.gainExpDisplay.listeners.Listeners;
import dev.punjarrr.gainExpDisplay.text.Messenger;
import dev.punjarrr.gainExpDisplay.utils.ConfigUpdater;
import dev.punjarrr.gainExpDisplay.utils.LanguageLoader;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class GainExpDisplay extends JavaPlugin {

    private volatile Settings settings;
    private LanguageLoader lang;
    private Messenger messenger;

    @Override
    public void onEnable() {
        new ConfigUpdater(getDataFolder(), getLogger()).migrateIfNeeded();
        saveDefaultConfig();

        lang = new LanguageLoader(this);
        loadSettings();

        messenger = Messenger.detect();
        getLogger().info("Sending messages with " + messenger.description());

        getServer().getPluginManager().registerEvents(new Listeners(this, lang, messenger), this);

        PluginCommand command = getCommand("gainexpdisplay");
        if (command != null) {
            GedCommand executor = new GedCommand(this, lang, messenger);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }
    }

    /** Reads config.yml and the chosen translation file again (startup and /ged reload). */
    public void loadSettings() {
        reloadConfig();
        Settings loaded = Settings.from(getConfig());
        lang.load(loaded.lang());
        settings = loaded;
    }

    public Settings settings() {
        return settings;
    }
}

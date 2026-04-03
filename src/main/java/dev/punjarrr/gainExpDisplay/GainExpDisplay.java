package dev.punjarrr.gainExpDisplay;

import dev.punjarrr.gainExpDisplay.commands.ged.GedCommand;
import dev.punjarrr.gainExpDisplay.listeners.Listeners;
import dev.punjarrr.gainExpDisplay.utils.LanguageLoader;
import org.bukkit.plugin.java.JavaPlugin;

public final class GainExpDisplay extends JavaPlugin {

    private LanguageLoader lang;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        lang = new LanguageLoader(this);
        lang.load(getConfig().getString("language", "en_US"));

        getServer().getPluginManager().registerEvents(new Listeners(this, lang), this);

        getCommand("gainexpdisplay").setExecutor(new GedCommand(this, lang));
        getCommand("gainexpdisplay").setTabCompleter(new GedCommand(this, lang));
    }

    @Override
    public void onDisable() {}
}

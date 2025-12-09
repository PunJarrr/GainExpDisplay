package dev.punjarrr.gainExpDisplay;

import dev.punjarrr.gainExpDisplay.listeners.Listeners;
import org.bukkit.plugin.java.JavaPlugin;

public final class GainExpDisplay extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(
                new Listeners(this),
                this
        );
    }

    @Override
    public void onDisable() {}
}

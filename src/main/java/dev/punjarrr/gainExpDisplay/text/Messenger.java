package dev.punjarrr.gainExpDisplay.text;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Sends formatted text to players and the console. The plugin builds every message with its own
 * (bundled) MiniMessage, then a Messenger hands it to the server in whatever form that server understands.
 */
public interface Messenger {

    void sendMessage(CommandSender to, Component message);

    void sendActionBar(Player to, Component message);

    /** Shown in the startup log, and checked by the smoke test. */
    String description();

    /** Paper, Purpur and Folia have Adventure text built in; plain Spigot only has BungeeCord chat. */
    static Messenger detect() {
        Messenger nativeMessenger = NativeMessenger.tryCreate();
        return nativeMessenger != null ? nativeMessenger : new BungeeMessenger();
    }
}

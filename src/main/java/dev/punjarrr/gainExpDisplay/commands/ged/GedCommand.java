package dev.punjarrr.gainExpDisplay.commands.ged;

import dev.punjarrr.gainExpDisplay.GainExpDisplay;
import dev.punjarrr.gainExpDisplay.text.Messenger;
import dev.punjarrr.gainExpDisplay.utils.LanguageLoader;
import dev.punjarrr.gainExpDisplay.utils.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GedCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION_RELOAD = "gainexpdisplay.reload";

    private final GainExpDisplay plugin;
    private final LanguageLoader lang;
    private final Messenger messenger;

    public GedCommand(GainExpDisplay plugin, LanguageLoader lang, Messenger messenger) {
        this.plugin = plugin;
        this.lang = lang;
        this.messenger = messenger;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission(PERMISSION_RELOAD)) {
                send(sender, "command-no-permission");
                return true;
            }
            plugin.loadSettings();
            send(sender, "reload");
            return true;
        }

        send(sender, "command-error");
        return true;
    }

    private void send(CommandSender sender, String key) {
        // Players get their own language when per-player-language is on; the console gets the server's
        String locale = sender instanceof Player player && plugin.settings().perPlayerLanguage() ? player.getLocale() : null;
        String format = lang.get(key, locale);
        if (!format.isEmpty()) messenger.sendMessage(sender, TextUtil.parse(format));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (args.length == 1 && sender.hasPermission(PERMISSION_RELOAD) && "reload".startsWith(args[0].toLowerCase())) {
            return List.of("reload");
        }
        return List.of();
    }
}

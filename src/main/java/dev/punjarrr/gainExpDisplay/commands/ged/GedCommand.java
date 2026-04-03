package dev.punjarrr.gainExpDisplay.commands.ged;

import dev.punjarrr.gainExpDisplay.GainExpDisplay;
import dev.punjarrr.gainExpDisplay.utils.LanguageLoader;
import dev.punjarrr.gainExpDisplay.utils.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GedCommand implements org.bukkit.command.CommandExecutor, TabCompleter {

    private final GainExpDisplay plugin;
    private final LanguageLoader lang;

    public GedCommand(GainExpDisplay plugin, LanguageLoader lang) {
        this.plugin = plugin;
        this.lang = lang;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {

            if (!sender.hasPermission("gainexpdisplay.reload")) {
                String format = lang.get("command-no-permission");
                if (format.isEmpty()) return true;

                sender.sendMessage(TextUtil.parse(format));

                return true;
            }

            plugin.reloadConfig();
            lang.load(plugin.getConfig().getString("lang", "en_US"));

            String format = lang.get("reload");
            if (format.isEmpty()) return true;

            sender.sendMessage(TextUtil.parse(format));

            return true;
        }

        String format = lang.get("command-error");
        if (format.isEmpty()) return true;

        sender.sendMessage(TextUtil.parse(format));

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>();

            if (sender.hasPermission("gainexpdisplay.reload")) {
                options.add("reload");
            }

            return options;
        }
        return List.of();
    }
}

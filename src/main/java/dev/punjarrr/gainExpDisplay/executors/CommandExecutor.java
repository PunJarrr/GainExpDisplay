package dev.punjarrr.gainExpDisplay.executors;

import dev.punjarrr.gainExpDisplay.GainExpDisplay;
import dev.punjarrr.gainExpDisplay.utils.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CommandExecutor implements org.bukkit.command.CommandExecutor, TabCompleter {

    private final GainExpDisplay plugin;

    public CommandExecutor(GainExpDisplay plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {

            if (!sender.hasPermission("gainexpdisplay.reload")) {
                String format = plugin.getConfig().getString("messages.command-no-permission", "");
                if (format.isEmpty()) return true;

                sender.sendMessage(TextUtil.parse(format));

                return true;
            }

            plugin.reloadConfig();

            String format = plugin.getConfig().getString("messages.reload", "");
            if (format.isEmpty()) return true;

            sender.sendMessage(TextUtil.parse(format));

            return true;
        }

        String format = plugin.getConfig().getString("messages.command-error", "");
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

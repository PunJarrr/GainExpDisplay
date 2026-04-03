package dev.punjarrr.gainExpDisplay.listeners;

import dev.punjarrr.gainExpDisplay.GainExpDisplay;
import dev.punjarrr.gainExpDisplay.utils.LanguageLoader;
import dev.punjarrr.gainExpDisplay.utils.TextUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

public class Listeners implements Listener {

    private final GainExpDisplay plugin;
    private final LanguageLoader lang;

    public Listeners(GainExpDisplay plugin, LanguageLoader lang) {
        this.plugin = plugin;
        this.lang = lang;
    }

    @EventHandler
    public void onExpChange(PlayerExpChangeEvent e) {
        if (!plugin.getConfig().getBoolean("gain-xp.enabled", true)) return;
        if (plugin.getConfig().getBoolean("require-permission", true)) {
            if (!e.getPlayer().hasPermission("gainexpdisplay.gainexp")) return;
        }

        Player player = e.getPlayer();
        int amount = e.getAmount();

        String format = lang.get("gain-xp");
        if (format.isEmpty()) return;

        player.sendActionBar(TextUtil.parse(format, amount));
    }

    @EventHandler
    public void onPlayerItemMend(PlayerItemMendEvent e) {
        if (!plugin.getConfig().getBoolean("mending-repair.enabled", true)) return;
        if (plugin.getConfig().getBoolean("require-permission", true)) {
            if (!e.getPlayer().hasPermission("gainexpdisplay.gainexp")) return;
        }

        Player player = e.getPlayer();
        int amount = e.getRepairAmount();
        ItemStack item = e.getItem();

        if (!(item.getItemMeta() instanceof Damageable damageable)) return;

        int maxDurability = item.getType().getMaxDurability();
        int durability = maxDurability - damageable.getDamage() + amount;

        String format = lang.get("mending-repair");
        if (format.isEmpty()) return;

        player.sendActionBar(TextUtil.parse(format, amount, durability, maxDurability));
    }
}

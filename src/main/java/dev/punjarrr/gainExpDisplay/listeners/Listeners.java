package dev.punjarrr.gainExpDisplay.listeners;

import dev.punjarrr.gainExpDisplay.utils.TextUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.plugin.java.JavaPlugin;

public class Listeners implements Listener {

    private final JavaPlugin plugin;

    public Listeners(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onExpChange(PlayerExpChangeEvent e) {
        if (!plugin.getConfig().getBoolean("gain-xp.enabled", true)) return;

        Player player = e.getPlayer();
        int amount = e.getAmount();

        String format = plugin.getConfig().getString("gain-xp.message", "");
        if (format.isEmpty()) return;

        player.sendActionBar(TextUtil.parse(format, amount));
    }

    @EventHandler
    public void onPlayerItemMend(PlayerItemMendEvent e) {
        if (!plugin.getConfig().getBoolean("mending-repair.enabled", true)) return;

        Player player = e.getPlayer();
        int amount = e.getRepairAmount();
        ItemStack item = e.getItem();

        if (!(item.getItemMeta() instanceof Damageable damageable)) return;

        int maxDurability = item.getType().getMaxDurability();
        int durability = maxDurability - damageable.getDamage() + amount;

        String format = plugin.getConfig().getString("mending-repair.message", "");
        if (format.isEmpty()) return;

        player.sendActionBar(TextUtil.parse(format, amount, durability, maxDurability));
    }
}

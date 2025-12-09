package dev.punjarrr.gainExpDisplay.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
    private final MiniMessage mm = MiniMessage.miniMessage();

    public Listeners(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onExpChange(PlayerExpChangeEvent e) {
        Player player = e.getPlayer();
        int amount = e.getAmount();

        String format = plugin.getConfig().getString("messages.gain-xp", "");
        if (format.isEmpty()) return;

        Component message = mm.deserialize(
                format.replace("%amount%", String.valueOf(amount))
        );

        player.sendActionBar(message);
    }

    @EventHandler
    public void onPlayerItemMend(PlayerItemMendEvent e) {
        Player player = e.getPlayer();
        int amount = e.getRepairAmount();
        ItemStack item = e.getItem();

        if (!(item.getItemMeta() instanceof Damageable damageable)) return;

        int maxDurability = item.getType().getMaxDurability();
        int durability = maxDurability - damageable.getDamage() + amount;

        String format = plugin.getConfig().getString("messages.mending-repair", "");
        if (format.isEmpty()) return;

        Component message = mm.deserialize(
                format
                        .replace("%amount%", String.valueOf(amount))
                        .replace("%durability%", String.valueOf(durability))
                        .replace("%max%", String.valueOf(maxDurability))
        );

        player.sendActionBar(message);
    }
}

package dev.punjarrr.gainExpDisplay.listeners;

import dev.punjarrr.gainExpDisplay.GainExpDisplay;
import dev.punjarrr.gainExpDisplay.Settings;
import dev.punjarrr.gainExpDisplay.text.Messenger;
import dev.punjarrr.gainExpDisplay.utils.Durability;
import dev.punjarrr.gainExpDisplay.utils.LanguageLoader;
import dev.punjarrr.gainExpDisplay.utils.TextUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

public class Listeners implements Listener {

    static final String PERMISSION_GAIN_XP = "gainexpdisplay.gainexp";
    static final String PERMISSION_MENDING = "gainexpdisplay.mending";

    private final GainExpDisplay plugin;
    private final LanguageLoader lang;
    private final Messenger messenger;

    public Listeners(GainExpDisplay plugin, LanguageLoader lang, Messenger messenger) {
        this.plugin = plugin;
        this.lang = lang;
        this.messenger = messenger;
    }

    // MONITOR: show the final amount, after any other plugin has changed it
    @EventHandler(priority = EventPriority.MONITOR)
    public void onExpChange(PlayerExpChangeEvent e) {
        Settings settings = plugin.settings();
        if (!settings.gainXp()) return;

        Player player = e.getPlayer();
        if (settings.requirePermission() && !player.hasPermission(PERMISSION_GAIN_XP)) return;

        int amount = e.getAmount();
        if (amount <= 0) return;

        String format = lang.get("gain-xp", localeOf(player, settings));
        if (format.isEmpty()) return;

        messenger.sendActionBar(player, TextUtil.parse(format, amount));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerItemMend(PlayerItemMendEvent e) {
        Settings settings = plugin.settings();
        if (!settings.mendingRepair()) return;

        Player player = e.getPlayer();
        if (settings.requirePermission() && !player.hasPermission(PERMISSION_MENDING)) return;

        int amount = e.getRepairAmount();
        if (amount <= 0) return;

        ItemStack item = e.getItem();
        if (!(item.getItemMeta() instanceof Damageable damageable)) return;

        int maxDurability = Durability.max(item, damageable);
        if (maxDurability <= 0) return;
        int durability = Durability.afterRepair(maxDurability, damageable.getDamage(), amount);

        String format = lang.get("mending-repair", localeOf(player, settings));
        if (format.isEmpty()) return;

        messenger.sendActionBar(player, TextUtil.parse(format, amount, durability, maxDurability));
    }

    /** The player's Minecraft language when per-player-language is on, otherwise null (the server's language). */
    static String localeOf(Player player, Settings settings) {
        return settings.perPlayerLanguage() ? player.getLocale() : null;
    }
}

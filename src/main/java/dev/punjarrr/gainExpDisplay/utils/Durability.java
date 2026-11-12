package dev.punjarrr.gainExpDisplay.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.lang.reflect.Method;

public final class Durability {

    // Damageable#hasMaxDamage / #getMaxDamage exist from 1.20.5, for items with a custom max durability
    private static final Method HAS_MAX_DAMAGE = findMethod("hasMaxDamage");
    private static final Method GET_MAX_DAMAGE = findMethod("getMaxDamage");

    private Durability() {}

    /** The item's maximum durability, including a custom one set by a data component on 1.20.5+. */
    public static int max(ItemStack item, Damageable meta) {
        if (HAS_MAX_DAMAGE != null && GET_MAX_DAMAGE != null) {
            try {
                if ((boolean) HAS_MAX_DAMAGE.invoke(meta)) return (int) GET_MAX_DAMAGE.invoke(meta);
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                // fall back to the material's durability
            }
        }
        return item.getType().getMaxDurability();
    }

    /**
     * Durability left after Mending repairs {@code repair} points. The event fires before the repair, and
     * Mending never repairs past full, so the result is capped at {@code max}.
     */
    public static int afterRepair(int max, int damage, int repair) {
        return Math.max(0, Math.min(max, max - damage + repair));
    }

    private static Method findMethod(String name) {
        try {
            return Damageable.class.getMethod(name);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}

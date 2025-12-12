package dev.punjarrr.gainExpDisplay.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class TextUtil {

    private static final MiniMessage mm = MiniMessage.miniMessage();

    public static Component parse(String input) {
        return mm.deserialize(input);
    }

    public static Component parse(String input, int amount) {
        return mm.deserialize(
                input.replace("%amount%", String.valueOf(amount))
        );
    }

    public static Component parse(String input, int amount, int durability, int maxDurability) {
        return mm.deserialize(
                input
                        .replace("%amount%", String.valueOf(amount))
                        .replace("%durability%", String.valueOf(durability))
                        .replace("%max%", String.valueOf(maxDurability))
        );
    }
}

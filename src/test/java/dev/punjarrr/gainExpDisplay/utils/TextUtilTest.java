package dev.punjarrr.gainExpDisplay.utils;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextUtilTest {

    private static String plain(net.kyori.adventure.text.Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    @Test
    void fillsInTheXpAmount() {
        assertEquals("Gain XP: +7", plain(TextUtil.parse("<green><bold>Gain XP:</bold> <yellow>+%amount%", 7)));
    }

    @Test
    void fillsInTheMendingPlaceholders() {
        assertEquals("Mending Repair: +4 (240/250)",
                plain(TextUtil.parse("<aqua>Mending Repair: +%amount% <green>(%durability%/%max%)", 4, 240, 250)));
    }
}

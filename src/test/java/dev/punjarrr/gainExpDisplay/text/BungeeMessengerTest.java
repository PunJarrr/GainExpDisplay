package dev.punjarrr.gainExpDisplay.text;

import dev.punjarrr.gainExpDisplay.utils.TextUtil;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BungeeMessengerTest {

    @Test
    void hexColoursUseTheFormatSpigotUnderstands() {
        // Same settings as BungeeMessenger: §x§r§r§g§g§b§b for hex colours
        LegacyComponentSerializer legacy = LegacyComponentSerializer.builder()
                .character(LegacyComponentSerializer.SECTION_CHAR).hexColors().useUnusualXRepeatedCharacterHexFormat().build();
        assertEquals("§x§1§2§3§4§5§6hi", legacy.serialize(TextUtil.parse("<#123456>hi")));
    }
}

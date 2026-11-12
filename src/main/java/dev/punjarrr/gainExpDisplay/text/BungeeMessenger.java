package dev.punjarrr.gainExpDisplay.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Plain Spigot: turns the message into colour-coded text (hex colours included, so gradients still work)
 * and sends the action bar with Spigot's BungeeCord chat API.
 */
final class BungeeMessenger implements Messenger {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .character(LegacyComponentSerializer.SECTION_CHAR)
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat() // the §x§r§r§g§g§b§b form Spigot understands
            .build();

    @Override
    public void sendMessage(CommandSender to, Component message) {
        to.sendMessage(LEGACY.serialize(message));
    }

    @Override
    public void sendActionBar(Player to, Component message) {
        to.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(LEGACY.serialize(message)));
    }

    @Override
    public String description() {
        return "BungeeCord chat (Spigot)";
    }
}

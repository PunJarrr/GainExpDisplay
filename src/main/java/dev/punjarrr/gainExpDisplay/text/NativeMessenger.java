package dev.punjarrr.gainExpDisplay.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.json.JSONOptions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Paper, Purpur and Folia: converts our bundled Component to the server's own Adventure Component (through
 * JSON) and sends it with the server's methods, so every text feature the server supports works.
 *
 * <p>The server's Adventure classes are looked up by name at runtime. The names are built from pieces because
 * the shadow plugin renames every "net.kyori" it finds in our jar to the bundled copy.
 */
final class NativeMessenger implements Messenger {

    private static final String ADVENTURE = String.join(".", "net", "kyori", "adventure");

    // The older JSON format, which every Adventure version since Paper 1.17 can read
    private final GsonComponentSerializer toJson = GsonComponentSerializer.builder()
            .editOptions(options -> options.values(JSONOptions.compatibility()))
            .build();

    private final Object serverSerializer;
    private final Method deserialize;
    private final Method sendMessage;
    private final Method sendActionBar;

    private NativeMessenger(Object serverSerializer, Method deserialize, Method sendMessage, Method sendActionBar) {
        this.serverSerializer = serverSerializer;
        this.deserialize = deserialize;
        this.sendMessage = sendMessage;
        this.sendActionBar = sendActionBar;
    }

    /** Returns null when the server has no built-in Adventure (plain Spigot). */
    static NativeMessenger tryCreate() {
        try {
            ClassLoader server = Bukkit.class.getClassLoader();
            Class<?> audience = Class.forName(ADVENTURE + ".audience.Audience", false, server);
            if (!audience.isAssignableFrom(Player.class) || !audience.isAssignableFrom(CommandSender.class)) {
                return null;
            }
            Class<?> component = Class.forName(ADVENTURE + ".text.Component", false, server);
            Class<?> gson = Class.forName(ADVENTURE + ".text.serializer.gson.GsonComponentSerializer", true, server);
            Object serializer = gson.getMethod("gson").invoke(null);
            Method deserialize = findMethod(gson, "deserialize", String.class);
            Method sendMessage = findMethod(audience, "sendMessage", component);
            Method sendActionBar = findMethod(audience, "sendActionBar", component);
            if (deserialize == null || sendMessage == null || sendActionBar == null) return null;
            return new NativeMessenger(serializer, deserialize, sendMessage, sendActionBar);
        } catch (ReflectiveOperationException | LinkageError e) {
            return null;
        }
    }

    /** A one-argument method that accepts {@code argument} (an exact match is preferred). */
    private static Method findMethod(Class<?> owner, String name, Class<?> argument) {
        try {
            return owner.getMethod(name, argument);
        } catch (NoSuchMethodException ignored) {
            // e.g. deserialize(Object) from the generic ComponentSerializer, or sendMessage(ComponentLike)
        }
        for (Method method : owner.getMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == 1
                    && method.getParameterTypes()[0].isAssignableFrom(argument)) {
                return method;
            }
        }
        return null;
    }

    private Object toServerComponent(Component message) throws ReflectiveOperationException {
        return deserialize.invoke(serverSerializer, toJson.serialize(message));
    }

    @Override
    public void sendMessage(CommandSender to, Component message) {
        invoke(sendMessage, to, message);
    }

    @Override
    public void sendActionBar(Player to, Component message) {
        invoke(sendActionBar, to, message);
    }

    private void invoke(Method method, Object target, Component message) {
        try {
            method.invoke(target, toServerComponent(message));
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtime) throw runtime;
            if (cause instanceof Error error) throw error;
            throw new IllegalStateException(cause);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not send a message with the server's Adventure", e);
        }
    }

    @Override
    public String description() {
        return "the server's built-in Adventure text (Paper)";
    }
}

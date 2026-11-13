package dev.punjarrr.gainExpDisplay.utils;

import java.util.Collection;
import java.util.Locale;

/** Picks which translation file a player gets from their Minecraft language setting. */
public final class LanguageMatcher {

    private LanguageMatcher() {}

    /**
     * @param clientLocale the player's language as the game reports it, such as {@code th_th} or {@code es_mx}
     * @param available    translation file names without {@code .yml}, such as {@code th_TH}
     * @param fallback     the server's configured language, used when nothing fits
     * @return the exact match ({@code th_th} → {@code th_TH}); otherwise a file in the same language
     *         ({@code es_mx} → {@code es_ES}); otherwise {@code fallback}
     */
    public static String match(String clientLocale, Collection<String> available, String fallback) {
        if (clientLocale == null || clientLocale.isBlank()) return fallback;
        String wanted = clientLocale.trim().replace('-', '_').toLowerCase(Locale.ROOT);

        for (String name : available) {
            if (name.equalsIgnoreCase(wanted)) return name;
        }

        String language = languageOf(wanted);
        if (languageOf(fallback).equals(language)) return fallback;
        return available.stream()
                .filter(name -> languageOf(name).equals(language))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .findFirst()
                .orElse(fallback);
    }

    private static String languageOf(String locale) {
        String lower = locale.toLowerCase(Locale.ROOT);
        int underscore = lower.indexOf('_');
        return underscore < 0 ? lower : lower.substring(0, underscore);
    }
}

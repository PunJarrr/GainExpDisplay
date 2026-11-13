package dev.punjarrr.gainExpDisplay.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LanguageMatcherTest {

    private static final List<String> BUNDLED = List.of("de_DE", "en_US", "es_ES", "fr_FR", "ja_JP", "th_TH", "zh_CN");

    @Test
    void exactMatchIgnoresCase() {
        // The game reports languages in lower case
        assertEquals("th_TH", LanguageMatcher.match("th_th", BUNDLED, "en_US"));
    }

    @Test
    void sameLanguageFromAnotherCountry() {
        assertEquals("es_ES", LanguageMatcher.match("es_mx", BUNDLED, "en_US"));
        assertEquals("fr_FR", LanguageMatcher.match("fr_ca", BUNDLED, "en_US"));
    }

    @Test
    void prefersTheServerLanguageWhenItIsTheSameLanguage() {
        // British English uses the server's en_US rather than another English file
        assertEquals("en_US", LanguageMatcher.match("en_gb", List.of("en_AU", "en_US"), "en_US"));
    }

    @Test
    void unknownLanguageUsesTheServerLanguage() {
        assertEquals("de_DE", LanguageMatcher.match("pt_br", BUNDLED, "de_DE"));
    }

    @Test
    void missingLanguageUsesTheServerLanguage() {
        assertEquals("ja_JP", LanguageMatcher.match(null, BUNDLED, "ja_JP"));
        assertEquals("ja_JP", LanguageMatcher.match("", BUNDLED, "ja_JP"));
    }

    @Test
    void customFilesWork() {
        assertEquals("pt_BR", LanguageMatcher.match("pt_br", List.of("en_US", "pt_BR"), "en_US"));
    }
}

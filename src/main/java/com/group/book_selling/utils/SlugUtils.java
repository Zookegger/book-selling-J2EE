package com.group.book_selling.utils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class SlugUtils {

    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");
    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]+");
    private static final Pattern EDGE_DASH = Pattern.compile("(^-+|-+$)");

    public static String slugify(String input) {
        if (input == null || input.isBlank()) {
            return "item";
        }

        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String withoutDiacritics = DIACRITICS.matcher(normalized).replaceAll("");
        String lower = withoutDiacritics.toLowerCase(Locale.ROOT);
        String withDashes = NON_ALNUM.matcher(lower).replaceAll("-");
        String trimmed = EDGE_DASH.matcher(withDashes).replaceAll("");

        return trimmed.isBlank() ? "item" : trimmed;
    }
}

package com.dev_high.search.util;

import java.util.*;
import java.util.regex.Pattern;

public final class EmbeddingTextPreprocessor {

    private EmbeddingTextPreprocessor() {
    }

    private static final Pattern MULTI_SPACE = Pattern.compile("[ \\t\\x0B\\f]+");

    private static final Pattern REMOVE_SPECIAL_CHARS = Pattern.compile("[\\[\\]{}()<>•·*\\-‒–—]");

    public static String preprocess(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }

        String[] lines = raw.replace("\uFEFF", "")
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .split("\n");

        List<String> cleanedLines = new ArrayList<>();

        for (String line : lines) {
            String l = line.trim();

            if (l.isEmpty()) {
                continue;
            }

            l = REMOVE_SPECIAL_CHARS.matcher(l).replaceAll("").trim();

            char lastChar = l.charAt(l.length() - 1);
            if (lastChar != '.' && lastChar != '!' && lastChar != '?' && lastChar != '…'
                    && lastChar != '。' && lastChar != '！' && lastChar != '？') {
                l += ".";
            }

            cleanedLines.add(l);
        }

        String result = String.join(" ", cleanedLines);
        result = MULTI_SPACE.matcher(result).replaceAll(" ").trim();

        return result;
    }
}
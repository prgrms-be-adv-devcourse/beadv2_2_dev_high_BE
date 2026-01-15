package com.dev_high.user.user.util;

public final class EmailMasker {
    private EmailMasker() {}

    public static String mask(String email) {
        if (email == null || email.isBlank()) {
            return email;
        }

        String[] parts = email.split("@");

        String local = parts[0];
        String domain = parts[1];

        if (local.length() <= 3) {
            return email;
        }

        return local.substring(0, 3) +
                "*".repeat(Math.max(0, local.length() - 3)) +
                '@' + domain;
    }
}
package com.tfmsp.backend.util;

public final class TextSanitizer {

    private TextSanitizer() {}

    /** Removes HTML tags like <script> or <b> and trims the text. */
    public static String clean(String text) {
        if (text == null) return null;
        return text.replaceAll("(?s)<[^>]*>", "").trim();
    }
}
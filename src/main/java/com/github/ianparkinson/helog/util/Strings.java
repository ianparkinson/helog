package com.github.ianparkinson.helog.util;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class Strings {
    private Strings() {
    }

    /**
     * Returns the input {@link String}, unless it is {@code null} or literally {@code "null"}, in which
     * case returns the empty {@link String}.
     */
    public static String emptyIfNull(String input) {
        return (input == null || input.equalsIgnoreCase("null")) ? "" : input;
    }

    /**
     * Returns the input {@link String}, unless it is {@code null}, literally {@code "null"}, or literally {@code "0"}.
     */
    public static String emptyIfNullOrZero(String input) {
        return (input == null || input.equalsIgnoreCase("null") || input.equals("0")) ? "" : input;
    }

    /**
     * Validates the {@code host} parameter, which is an IP address or hostname, with an optional port number seperated
     * by a colon. This isn't intended to be a strict test of whether the input string is a valid IP address or host
     * name it's just to prevent accidental use of entire URLs etc.
     */
    public static boolean isHostPort(String input) {
        return input.matches("[^:/@?&]+(:\\d+)?");
    }

    /**
     * Tests the given string to see if it can be parsed as an int.
     */
    public static boolean isInteger(String input) {
        if (input == null) {
            return false;
        }
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Renders a list of strings as a line of CSV, absent a trailing newline.
     */
    public static String csvLine(List<String> strings) {
        return strings.stream().map(Strings::csvEscapeValue).collect(Collectors.joining(","));
    }

    private static final Pattern csvSpecialChars = Pattern.compile("[\",\\r\\n\\t]");
    private static final Pattern doubleQuotes = Pattern.compile("\"");
    private static String csvEscapeValue(String value) {
        // Simplified implementation of CSV escaping. If the value contains any special characters (such as newlines,
        // commas), the whole string is encased in double-quotes. Existing double-quotes (") are doubled up ("").
        if (csvSpecialChars.matcher(value).find()) {
            return '"' + doubleQuotes.matcher(value).replaceAll("\"\"") + '"';
        } else {
           return value;
        }
    }
}

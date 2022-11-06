package com.github.ianparkinson.helog;

final class Strings {
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
     * Validates the {@code host} parameter, which is an IP address or hostname, with an optional port number seperated
     * by a colon. This isn't intended to be a strict test of whether the input string is a valid IP address or host
     * name it's just to prevent accidental use of entire URLs etc.
     */
    public static boolean isHostPort(String input) {
        return input.matches("[^:/@?&]+(:\\d+)?");
    }
}

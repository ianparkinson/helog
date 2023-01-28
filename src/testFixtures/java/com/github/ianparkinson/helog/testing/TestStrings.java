package com.github.ianparkinson.helog.testing;

public final class TestStrings {
    private TestStrings() {
    }

    /**
     * Constructs a String consisting of the given {@code lines}, each terminated by the system newline.
     */
    public static String lines(String... lines) {
        StringBuilder builder = new StringBuilder();
        for (String line : lines) {
            builder.append(line);
            builder.append(System.lineSeparator());
        }
        return builder.toString();
    }

    /**
     * Splits a String into lines, using any Unicode line separator.
     */
    public static String[] splitLines(String string) {
        return string.split("\\R");
    }

    /**
     * Regex matching the output of {@link
     * com.github.ianparkinson.helog.util.DateTimeFormatters#ISO_OFFSET_DATE_TIME_MILLIS}.
     */
    public static String ISO_OFFSET_DATE_TIME_MILLIS_REGEX =
            "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}(Z|(\\+|-)\\d{2}:\\d{2})";
}

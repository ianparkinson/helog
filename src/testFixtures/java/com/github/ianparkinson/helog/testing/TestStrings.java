package com.github.ianparkinson.helog.testing;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TestStrings {

    /**
     * Pattern matching the output of {@link
     * com.github.ianparkinson.helog.util.DateTimeFormatters#ISO_OFFSET_DATE_TIME_MILLIS}.
     */
    private static final Pattern ISO_OFFSET_DATE_TIME_MILLIS_PATTERN = Pattern.compile(
            "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}(Z|([+\\-])\\d{2}:\\d{2})"
    );

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
     * Extracts and parses the datetime portion of a log record, which is assumed to be at the very start of the
     * supplied {@link String}.
     *
     * @throws DateTimeParseException if the log record doesn't appear to start with a datetime string as produced by
     * {@link com.github.ianparkinson.helog.util.DateTimeFormatters#ISO_OFFSET_DATE_TIME_MILLIS}.
     */
    public static OffsetDateTime extractDateTime(String line) {
        Matcher matcher = ISO_OFFSET_DATE_TIME_MILLIS_PATTERN.matcher(line);
        if (!matcher.find()) {
            throw new DateTimeParseException("No DateTime string found", line, 0);
        }
        String dateField = line.substring(matcher.start(), matcher.end());
        return OffsetDateTime.parse(dateField);
    }

    /**
     * Removes the datetime portion of a log record, which is assumed to be at the very start of the supplied {@link
     * String}, and returns the remaining part of the String.
     *
     * @throws DateTimeParseException if the log record doesn't appear to start with a datetime string as produced by
     *      * {@link com.github.ianparkinson.helog.util.DateTimeFormatters#ISO_OFFSET_DATE_TIME_MILLIS}.
     */
    public static String dropDateTime(String line) {
        Matcher matcher = ISO_OFFSET_DATE_TIME_MILLIS_PATTERN.matcher(line);
        if (!matcher.find()) {
            throw new DateTimeParseException("No DateTime string found", line, 0);
        }
        return line.substring(matcher.end());
    }
}

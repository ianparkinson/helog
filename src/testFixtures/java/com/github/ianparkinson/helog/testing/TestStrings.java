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
}

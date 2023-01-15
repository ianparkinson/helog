package com.github.ianparkinson.helog.util;

import picocli.CommandLine.Help.Ansi;

/**
 * Encapsulates an error message, to be displayed to the user.
 *
 * <p>Includes:
 * <ul>
 *     <li>A message, which is assumed to be a compile-time constant and safe to be colourized using
 *     {@link Ansi}</li>
 *     <li>Optionally, a detail string, which will not be colourized.</li>
 * </ul>
 */
public final class ErrorMessage {
    private final String message;
    private final String detail;

    private ErrorMessage(String message, String detail) {
        this.message = message;
        this.detail = detail;
    }

    public String toString(Ansi ansi) {
        if (detail != null) {
            return ansi.string(String.format("@|red,bold %s:|@ %s", message, detail));
        } else {
            return ansi.string(String.format("@|red,bold %s|@", message));
        }
    }

    @Override
    public String toString() {
        return toString(Ansi.OFF);
    }

    public void writeToStderr(Ansi ansi) {
        System.err.println(toString(ansi));
    }

    public static ErrorMessage errorMessage(String message) {
        return new ErrorMessage(message, null);
    }

    public static ErrorMessage errorMessage(String message, String detailFormat, Object... detailArgs) {
        return new ErrorMessage(message, String.format(detailFormat, detailArgs));
    }
}

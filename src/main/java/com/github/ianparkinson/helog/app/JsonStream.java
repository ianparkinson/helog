package com.github.ianparkinson.helog.app;

import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Encapsulates information and utilities relating to a JSON event stream published, via a websocket, by a Hubitat
 * Elevation.
 *
 * @see EventsJsonStream
 * @see LogJsonStream
 *
 * @param <T> Event type representing an entry in the stream.
 */
public interface JsonStream<T> {
    /** The usual path to the websocket. */
    String path();

    /** {@link TypeToken} representing an entry in the stream. */
    TypeToken<T> type();

    /**
     * Filter by device.
     *
     * <p>{@code device} may be either the full name of a device, or the String representation of its numeric id.
     */
    Predicate<T> device(String device);

    /**
     * Filter by app.
     *
     * <p>For {@code logsocket}, {@code app} may be either the full name of an app or the String representation of its
     * numeric id. For {@code eventsocket}, it must be the numeric id.
     */
    Predicate<T> app(String app);

    /**
     * Filter by event name.
     *
     * <p>Only supported by {@code eventsocket}.
     */
    Predicate<T> eventName(String name);

    /**
     * Filter by log level.
     *
     * <p>Only supported by {@code logsocket}.</p>
     */
    Predicate<T> logLevel(String level);

    /** Renders an entry in the stream to a human-readable line of text. */
    Function<T, String> formatter();

    /** A header row, used with CSV format. */
    List<String> csvHeader();

    /** Renders an entry as a list of strings for use with CSV format. */
    Function<T, List<String>> csvFormatter();
}

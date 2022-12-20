package com.github.ianparkinson.helog;

import com.google.gson.reflect.TypeToken;

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
     * Filter by device. {@code device} may be either the full name of a device, or the String representation of its
     * numeric id.
     */
    Predicate<T> device(String device);

    /** Renders an entry in the stream to a human-readable line of text. */
    Function<T, String> formatter();
}

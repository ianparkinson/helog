package com.github.ianparkinson.helog.app;

import java.time.ZonedDateTime;

/**
 * Formats log entries.
 *
 * @param <T> Event type representing an entry in the stream.
 * @param <O> Output object.
 */
@FunctionalInterface
public interface JsonStreamFormatter<T, O> {
    O format(ZonedDateTime dateTime, T entry);
}

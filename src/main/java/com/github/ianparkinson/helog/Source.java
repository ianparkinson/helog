package com.github.ianparkinson.helog;

import java.io.Reader;

/**
 * Abstraction for connecting to, and reading from a source (typically a websocket).
 */
public interface Source {
    /**
     * Connect to the source.
     */
    Connection connect();

    interface Connection {
        /**
         * Provides access to content from the source.
         *
         * <p>The {@link Reader} is assumed to only ever close in error, in which case {@link #getError()} provides
         * information about the error.
         */
        Reader getReader();

        /**
         * A human-readable message describing the reason why the {@link Reader} has closed. {@code null} if not
         * available.
         */
        ErrorMessage getError();
    }
}

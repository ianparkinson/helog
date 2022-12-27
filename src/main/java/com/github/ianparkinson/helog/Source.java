package com.github.ianparkinson.helog;

import java.io.Reader;

/**
 * Abstraction for connecting to, and reading from a source (typically a websocket).
 */
public interface Source {
    /**
     * Connect to the source. Blocks until the connection is established.
     */
    Connection connect() throws ConnectionFailedException, InterruptedException;

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

    final class ConnectionFailedException extends Exception {
        public final ErrorMessage errorMessage;

        public ConnectionFailedException(ErrorMessage message) {
            super(message.toString());
            errorMessage = message;
        }

        public ConnectionFailedException(ErrorMessage message, Throwable cause) {
            super(message.toString(), cause);
            errorMessage = message;
        }
    }
}

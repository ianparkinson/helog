package com.github.ianparkinson.helog.app;

import com.github.ianparkinson.helog.util.ErrorMessage;

import java.net.URI;

/**
 * Simplified abstraction for a client which attaches to a websocket and receives text.
 */
public interface TextWebSocketClient {
    /**
     * Connect to the given {@link URI}. Events will be emitted through the provided {@link Listener} until the
     * connection fails.
     */
    void connect(URI uri, Listener listener);

    interface Listener {
        /** Called when the connection to the source is successfully established. */
        void onOpen();

        /** Called when a packet of text data is received. */
        void onText(CharSequence text, boolean last);

        /**
         * Called when an error occurs, either because the connection failed, or the source indicated an error, or
         * the connection was closed.
         */
        void onError(ErrorMessage error);
    }
}

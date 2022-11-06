package com.github.ianparkinson.helog;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;

/**
 * Connects to a WebSocket and spools the received text via a {@link PipedReader}.
 */
public final class WebSocketSource implements Source {
    private final URI uri;

    public WebSocketSource(URI uri) {
        this.uri = uri;
    }

    @Override
    public Connection connect() {
        WebSocketConnection connection = new WebSocketConnection();

        HttpClient.newHttpClient().newWebSocketBuilder().buildAsync(uri, new WebSocket.Listener() {
            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                try {
                    connection.writer.append(data);
                    connection.writer.flush();
                } catch (IOException e) {
                    // Ignore this - probably, the pipe is closed.
                }
                return WebSocket.Listener.super.onText(webSocket, data, last);
            }

            @Override
            public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                connection.closeOnError(String.format("WebSocket closed: %d \"%s\"%n", statusCode, reason));
                return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
            }

            @Override
            public void onError(WebSocket webSocket, Throwable error) {
                connection.closeOnError(String.format("WebSocket reported error: \"%s\"", error.getMessage()));
                WebSocket.Listener.super.onError(webSocket, error);
            }
        }).exceptionally(throwable -> {
            connection.closeOnError(String.format("Failed to connect: \"%s\"", throwable.getMessage()));
            return null;
        });

        return connection;
    }

    public static class WebSocketConnection implements Connection {
        private final PipedReader reader;
        private final PipedWriter writer;
        private volatile String error = null;

        private WebSocketConnection() {
            reader = new PipedReader(1024);
            writer = new PipedWriter();

            try {
                reader.connect(writer);
            } catch (IOException e) {
                throw new Error(e);
            }
        }

        @Override
        public Reader getReader() {
            return reader;
        }

        @Override
        public String getError() {
            return error;
        }

        private void closeOnError(String error) {
            this.error = error;
            try {
                writer.close();
            } catch (IOException e) {
                // Ignore this - probably the Writer is already closed.
            }
        }
    }
}

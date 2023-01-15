package com.github.ianparkinson.helog.app;

import com.github.ianparkinson.helog.util.ErrorMessage;
import picocli.CommandLine.Help.Ansi;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.github.ianparkinson.helog.util.ErrorMessage.errorMessage;

/**
 * Connects to a WebSocket and spools the received text via a {@link PipedReader}.
 */
public final class WebSocketSource implements Source {
    private final Ansi ansi;

    private final URI uri;

    public WebSocketSource(Ansi ansi, URI uri) {
        this.ansi = ansi;
        this.uri = uri;
    }

    @Override
    public Connection connect() throws ConnectionFailedException, InterruptedException {
        WebSocketConnection connection = new WebSocketConnection();
        HttpClient client = HttpClient.newHttpClient();
        Future<WebSocket> futureSocket = client.newWebSocketBuilder().buildAsync(uri, new WebSocket.Listener() {
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
                connection.closeOnError("WebSocket closed", "%d %s", statusCode, reason);
                return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
            }

            @Override
            public void onError(WebSocket webSocket, Throwable error) {
                connection.closeOnError("WebSocket reported error", "%s", error.getMessage());
                WebSocket.Listener.super.onError(webSocket, error);
            }
        });

        try {
            futureSocket.get();
            System.err.printf(ansi.string("@|blue Connected to %s|@%n"), uri);
            return connection;
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            ErrorMessage message = (cause != null
                    ? ErrorMessage.errorMessage("Failed to connect", "%s", cause.getMessage())
                    : ErrorMessage.errorMessage("Failed to connect"));
            throw new ConnectionFailedException(message, cause);
        } catch (CancellationException e) {
            // Nobody should be cancelling this future
            throw new RuntimeException("Unexpected cancellation", e);
        }
    }

    public static class WebSocketConnection implements Connection {
        private final PipedReader reader;
        private final PipedWriter writer;
        private volatile ErrorMessage error = null;

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
        public ErrorMessage getError() {
            return error;
        }

        private void closeOnError(String message, String detailFormat, Object... detailArgs) {
            this.error = errorMessage(message, detailFormat, detailArgs);
            try {
                writer.close();
            } catch (IOException e) {
                // Ignore this - probably the Writer is already closed.
            }
        }
    }
}

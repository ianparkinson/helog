package com.github.ianparkinson.helog.testing;

import com.github.ianparkinson.helog.Helog;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides a web socket server for tests. Content (which should be added to the {@code content} member) will be
 * sent to the client immediately upon connection, and then the socket will be closed.
 */
public final class WebSocketServerExtension implements BeforeEachCallback, AfterEachCallback {

    private final MockWebServer webServer = new MockWebServer();

    /**
     * Content to be set to the client as soon as it connects. Each entry will be sent as a text message.
     */
    public final List<String> content = new ArrayList<>();

    @Override
    public void beforeEach(ExtensionContext context) throws IOException {
        webServer.enqueue(new MockResponse().withWebSocketUpgrade(new TestWebSocketListener()));
        webServer.start();
    }

    @Override
    public void afterEach(ExtensionContext context) throws IOException {
        webServer.close();
    }

    /**
     * Returns the server's host and port, in the form {@code host:port}, as required by the {@link Helog}
     * command line.
     */
    public String getHostAndPort() {
        return webServer.getHostName() + ":" + webServer.getPort();
    }

    /**
     * Returns the next HTTP request, blocking if necessary.
     *
     * @see MockWebServer#takeRequest()
     */
    public RecordedRequest takeRequest() throws InterruptedException {
        return webServer.takeRequest();
    }

    public void close() throws IOException {
        webServer.close();
    }

    private final class TestWebSocketListener extends WebSocketListener {
        @Override
        public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
            for (String packet : content) {
                webSocket.send(packet);
            }
            webSocket.close(1000, null);
        }
    }
}

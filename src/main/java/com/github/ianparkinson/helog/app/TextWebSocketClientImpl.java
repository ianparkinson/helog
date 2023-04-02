package com.github.ianparkinson.helog.app;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;

import static com.github.ianparkinson.helog.util.ErrorMessage.errorMessage;

public final class TextWebSocketClientImpl implements TextWebSocketClient {
    @Override
    public void connect(URI uri, Listener listener) {
        HttpClient client = HttpClient.newHttpClient();
        client.newWebSocketBuilder().buildAsync(uri, new WebSocket.Listener() {
            @Override
            public void onOpen(WebSocket webSocket) {
                listener.onOpen();
                WebSocket.Listener.super.onOpen(webSocket);
            }

            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                listener.onText(data, last);
                return WebSocket.Listener.super.onText(webSocket, data, last);
            }

            @Override
            public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                listener.onError(errorMessage(
                        "WebSocket closed", "%d %s", statusCode, reason));
                return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
            }

            @Override
            public void onError(WebSocket webSocket, Throwable throwable) {
                listener.onError(errorMessage(
                        "WebSocket reported error", "%s", throwable.getMessage()));
                WebSocket.Listener.super.onError(webSocket, throwable);
            }
        }).exceptionally(throwable -> {
            if (throwable == null) {
                listener.onError(errorMessage("Failed to connect"));
            } else {
                listener.onError(errorMessage("Failed to connect", "%s", throwable.getMessage()));
            }
            return null;
        });
    }
}

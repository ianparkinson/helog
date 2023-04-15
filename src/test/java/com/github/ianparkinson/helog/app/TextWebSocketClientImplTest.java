package com.github.ianparkinson.helog.app;

import com.github.ianparkinson.helog.app.TextWebSocketClient.Listener;
import com.github.ianparkinson.helog.testing.WebSocketServerExtension;
import com.github.ianparkinson.helog.util.ErrorMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import static com.google.common.truth.Truth.assertThat;

final class TextWebSocketClientImplTest {
    @RegisterExtension
    final WebSocketServerExtension webServer = new WebSocketServerExtension();

    private final TextWebSocketClientImpl client = new TextWebSocketClientImpl();
    private final RecordingListener listener = new RecordingListener();

    @Test
    void reportsOpen() throws InterruptedException {
        client.connect(uri(), listener);
        assertThat(listener.event(0).eventType).isEqualTo(EventType.OPEN);
    }

    @Test
    void reportsClose() throws InterruptedException {
        client.connect(uri(), listener);
        assertThat(listener.event(1).eventType).isEqualTo(EventType.ERROR);
    }

    @Test
    void reportsData() throws InterruptedException {
        webServer.content.add("foo");
        client.connect(uri(), listener);
        assertThat(listener.event(1).eventType).isEqualTo(EventType.TEXT);
        assertThat(listener.event(1).text).isEqualTo("foo");
    }

    @Test
    void reportsConnectionFailure() throws InterruptedException, IOException {
        webServer.close();
        client.connect(uri(), listener);
        assertThat(listener.event(0).eventType).isEqualTo(EventType.ERROR);
    }

    private URI uri() {
        return URI.create("ws://" + webServer.getHostAndPort());
    }

    private static final class RecordingListener implements Listener {
        private final ArrayList<RecordedEvent> events = new ArrayList<>();
        private final CountDownLatch latch = new CountDownLatch(1);
        private StringBuilder stringBuilder = new StringBuilder();

        @Override
        public void onOpen() {
            events.add(new RecordedEvent(EventType.OPEN, null));
        }

        @Override
        public void onText(CharSequence text, boolean last) {
            stringBuilder.append(text);
            if (last) {
                events.add(new RecordedEvent(EventType.TEXT, stringBuilder.toString()));
                stringBuilder = new StringBuilder();
            }
        }

        @Override
        public void onError(ErrorMessage error) {
            events.add(new RecordedEvent(EventType.ERROR, null));
            latch.countDown();
        }

        public RecordedEvent event(int index) throws InterruptedException {
            latch.await();
            return events.get(index);
        }
    }

    private enum EventType { OPEN, TEXT, ERROR }

    private static final class RecordedEvent {
        private final EventType eventType;
        private final String text;

        private RecordedEvent(EventType eventType, String text) {
            this.eventType = eventType;
            this.text = text;
        }
    }
}

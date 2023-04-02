package com.github.ianparkinson.helog.app;

import com.github.ianparkinson.helog.util.ErrorMessage;
import com.google.gson.JsonSyntaxException;
import picocli.CommandLine.Help.Ansi;

import java.net.URI;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.concurrent.CountDownLatch;

import static com.github.ianparkinson.helog.util.ErrorMessage.errorMessage;

/**
 * Reads a stream of events from a URI, filters them, formats them, and writes them to stdout.
 *
 * <p>Forms the main loop for Helog, and blocks until the connection fails.
 */
public final class StreamPrinter {
    private final Clock clock;
    private final Ansi ansi;
    private final TextWebSocketClient client;

    public StreamPrinter(Clock clock, Ansi ansi, TextWebSocketClient client) {
        this.clock = clock;
        this.ansi = ansi;
        this.client = client;
    }

    /**
     * Read a stream of events from a URI, filters them, formats them and writes them to stdout.
     *
     * @param uri The {@link URI} to connect to.
     * @param header A line which will be printed after connecting, but before any events.
     * @param renderer Filters and formats the raw data received from the server.
     */
    public Streamer stream(URI uri, String header, Renderer renderer) {
        Streamer streamer = new Streamer(uri, header, renderer);
        streamer.run();
        return streamer;
    }

    public class Streamer {
        private final URI uri;
        private final String header;
        private final Renderer renderer;

        private final CountDownLatch errorLatch = new CountDownLatch(1);
        private StringBuilder builder = new StringBuilder();
        private ZonedDateTime dateTime = null;

        private Streamer(URI uri, String header, Renderer renderer) {
            this.uri = uri;
            this.header = header;
            this.renderer = renderer;
        }

        private void run() {
            client.connect(uri, new TextWebSocketClient.Listener() {
                @Override
                public void onOpen() {
                    System.err.printf(ansi.string("@|blue Connected to %s|@%n"), uri);
                    if (header != null) {
                        System.out.println(header);
                    }
                }

                @Override
                public void onText(CharSequence text, boolean last) {
                    builder.append(text);
                    if (dateTime == null) {
                        dateTime = ZonedDateTime.now(clock);
                    }
                    if (last) {
                        try {
                            String rendered = renderer.render(dateTime, builder.toString());
                            if (rendered != null) {
                                System.out.println(rendered);
                            }
                        } catch (JsonSyntaxException e) {
                            errorMessage("Malformed JSON", "%s", e.getMessage()).writeToStderr(ansi);
                            System.err.println(text);
                        }
                        dateTime = null;
                        builder = new StringBuilder();
                    }
                }

                @Override
                public void onError(ErrorMessage errorMessage) {
                    System.err.println(errorMessage);
                    errorLatch.countDown();
                }
            });
        }

        /**
         * Blocks until the connection fails.
         */
        public void waitUntilError() throws InterruptedException {
            errorLatch.await();
        }
    }

    /** Filters and formats textual events. */
    public interface Renderer {
        /**
         * Filter and format an event.
         *
         * @param dateTime The time at which the event was received.
         * @param text The received event payload.
         * @return The event as rendered for output, or {@code null} if the event should not be written.
         * @throws JsonSyntaxException If the event failed to parse.
         */
        String render(ZonedDateTime dateTime, String text) throws JsonSyntaxException;
    }
}

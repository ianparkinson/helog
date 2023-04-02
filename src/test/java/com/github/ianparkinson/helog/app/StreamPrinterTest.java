package com.github.ianparkinson.helog.app;

import com.github.ianparkinson.helog.testing.StdErrExtension;
import com.github.ianparkinson.helog.testing.StdOutExtension;
import com.google.gson.JsonSyntaxException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import picocli.CommandLine;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

import static com.github.ianparkinson.helog.testing.TestStrings.splitLines;
import static com.github.ianparkinson.helog.util.ErrorMessage.errorMessage;
import static com.google.common.truth.Truth.assertThat;

public class StreamPrinterTest {
    private static final ZonedDateTime DATE_TIME = ZonedDateTime.parse("2023-01-28T13:00Z");

    @RegisterExtension
    public final StdOutExtension out = new StdOutExtension();
    @RegisterExtension
    public final StdErrExtension err = new StdErrExtension();

    private static final URI uri = URI.create("ws://example.com");

    private final FakeClock clock = new FakeClock(DATE_TIME);
    private final FakeClient client = new FakeClient();
    private final StreamPrinter printer = new StreamPrinter(clock, CommandLine.Help.Ansi.OFF, client);

    @Test
    public void connectsToUri() {
        printer.stream(uri, null, (dateTime, text) -> text);
        assertThat(client.uri).isEqualTo(uri);
    }

    @Test
    public void signalsConnected() {
        printer.stream(uri, null, (dateTime, text) -> text);
        client.listener.onOpen();
        assertThat(splitLines(err.getContent())).containsExactly("Connected to " + uri);
    }

    @Test
    public void noHeader() {
        printer.stream(uri, null, (dateTime, text) -> text);
        client.listener.onOpen();
        assertThat(out.getContent()).isEmpty();
    }

    @Test
    public void noHeaderBeforeConnected() {
        printer.stream(uri, "Some Header", (dateTime, text) -> text);
        assertThat(out.getContent()).isEmpty();
    }

    @Test
    public void writesHeaderWhenConnected() {
        printer.stream(uri, "Some Header", (dateTime, text) -> text);
        client.listener.onOpen();
        assertThat(splitLines(out.getContent())).containsExactly("Some Header");
    }

    @Test
    public void formatsText() {
        printer.stream(uri, null, (dateTime, text) -> text.toUpperCase());
        client.listener.onOpen();
        client.listener.onText("some text", true);

        assertThat(splitLines(out.getContent())).containsExactly("SOME TEXT");
    }

    @Test
    public void combinesPartialTextEvents() {
        printer.stream(uri, null, (dateTime, text) -> text.toUpperCase());
        client.listener.onOpen();
        client.listener.onText("some ", false);
        client.listener.onText("text", true);

        assertThat(splitLines(out.getContent())).containsExactly("SOME TEXT");
    }

    @Test
    public void multipleTextEvents() {
        printer.stream(uri, null, (dateTime, text) -> text.toUpperCase());
        client.listener.onOpen();
        client.listener.onText("some", true);
        client.listener.onText("text", true);

        assertThat(splitLines(out.getContent())).containsExactly("SOME", "TEXT").inOrder();
    }

    @Test
    public void textEventFollowingSplitText() {
        printer.stream(uri, null, (dateTime, text) -> text.toUpperCase());
        client.listener.onOpen();
        client.listener.onText("some ", false);
        client.listener.onText("text", true);
        client.listener.onText("more text", true);

        assertThat(splitLines(out.getContent())).containsExactly("SOME TEXT", "MORE TEXT").inOrder();
    }

    @Test
    public void filtersEvent() {
        StreamPrinter.Renderer renderer = (dateTime, text) -> {
            if (text.contains("no")) {
                return null;
            } else {
                return text;
            }
        };
        printer.stream(uri, null, renderer);
        client.listener.onOpen();
        client.listener.onText("yes", true);
        client.listener.onText("no", true);
        client.listener.onText("yes", true);

        assertThat(splitLines(out.getContent())).containsExactly("yes", "yes").inOrder();
    }

    @Test
    public void providesTimestamp() {
        printer.stream(uri, null, (dateTime, text) -> dateTime.toString());
        client.listener.onOpen();
        client.listener.onText("text", true);

        assertThat(splitLines(out.getContent())).containsExactly("2023-01-28T13:00Z");
    }

    @Test
    public void updatesTimestamp() {
        printer.stream(uri, null, (dateTime, text) -> dateTime.toString());
        client.listener.onOpen();
        client.listener.onText("text", true);
        clock.advance(1, ChronoUnit.MINUTES);
        client.listener.onText("text", true);

        assertThat(splitLines(out.getContent()))
                .containsExactly("2023-01-28T13:00Z", "2023-01-28T13:01Z")
                .inOrder();
    }

    @Test
    public void usesEarliestTimestampOfSplitEvents() {
        printer.stream(uri, null, (dateTime, text) -> dateTime.toString());
        client.listener.onOpen();
        client.listener.onText("text", false);
        clock.advance(1, ChronoUnit.MINUTES);
        client.listener.onText("text", true);

        assertThat(splitLines(out.getContent()))
                .containsExactly("2023-01-28T13:00Z")
                .inOrder();
    }

    @Test
    public void reportsJsonSyntaxException() {
        StreamPrinter.Renderer renderer = (dateTime, text) -> {
            if (text.contains("bad")) {
                throw new JsonSyntaxException("test exception");
            } else {
                return text;
            }
        };
        printer.stream(uri, null, renderer);

        client.listener.onOpen();
        client.listener.onText("good text", true);
        client.listener.onText("bad text", true);
        client.listener.onText("good text", true);

        assertThat(splitLines(out.getContent())).containsExactly("good text", "good text").inOrder();
        assertThat(splitLines(err.getContent())).containsExactly(
                "Connected to " + uri,
                        "Malformed JSON: test exception",
                        "bad text")
                .inOrder();
    }

    @Test
    public void reportsConnectionError() {
        printer.stream(uri, null, (dateTime, text) -> text);
        client.listener.onError(errorMessage("test error"));
        assertThat(splitLines(out.getContent())).isEmpty();
        assertThat(splitLines(err.getContent())).containsExactly("test error");
    }

    @Test
    public void reportsErrorAfterConnection() {
        printer.stream(uri, null, (dateTime, text) -> text);
        client.listener.onOpen();
        client.listener.onError(errorMessage("test error"));
        assertThat(splitLines(out.getContent())).isEmpty();
        assertThat(splitLines(err.getContent())).containsExactly("Connected to " + uri, "test error").inOrder();
    }

    private static final class FakeClient implements TextWebSocketClient {
        public URI uri;
        public Listener listener;

        @Override
        public void connect(URI uri, Listener listener) {
            this.uri = uri;
            this.listener = listener;
        }
    }

    private static final class FakeClock extends Clock {
        private final ZoneId zoneId;
        private Instant instant;

        public FakeClock(ZonedDateTime dateTime) {
            this.zoneId = dateTime.getZone();
            this.instant = dateTime.toInstant();
        }

        public FakeClock(ZoneId zoneId, Instant instant) {
            this.zoneId = zoneId;
            this.instant = instant;
        }

        @Override
        public ZoneId getZone() {
            return zoneId;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new FakeClock(zone, instant);
        }

        @Override
        public Instant instant() {
            return instant;
        }

        public void advance(long amountToAdd, TemporalUnit unit) {
            instant = instant.plus(amountToAdd, unit);
        }
    }
}

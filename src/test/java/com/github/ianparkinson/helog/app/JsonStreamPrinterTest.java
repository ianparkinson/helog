package com.github.ianparkinson.helog.app;

import com.github.ianparkinson.helog.testing.FailedConnectionSource;
import com.github.ianparkinson.helog.testing.FixedContentSource;
import com.github.ianparkinson.helog.testing.StdErrExtension;
import com.github.ianparkinson.helog.testing.StdOutExtension;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import picocli.CommandLine.Help.Ansi;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.function.Predicate;

import static com.github.ianparkinson.helog.util.ErrorMessage.errorMessage;
import static com.github.ianparkinson.helog.testing.TestStrings.lines;
import static com.google.common.truth.Truth.assertThat;

public final class JsonStreamPrinterTest {

    private static final String timeString = "2023-01-28T13:00Z";

    @RegisterExtension
    private final StdOutExtension out = new StdOutExtension();
    @RegisterExtension
    private final StdErrExtension err = new StdErrExtension();

    private final Clock clock = Clock.fixed(ZonedDateTime.parse(timeString).toInstant(), ZoneId.of("Z"));

    @Test
    public void formatsEntry() {
        String content = "{\"name\": \"foo\", \"value\": 42}";
        jsonStreamPrinter().run(new FixedContentSource(content));

        assertThat(out.getContent()).isEqualTo(lines(timeString + " foo 42"));
    }

    @Test
    public void formatsMultipleEntries() {
        String content = "{\"name\": \"foo\", \"value\": 42}{\"name\": \"bar\", \"value\": 23}";
        jsonStreamPrinter().run(new FixedContentSource(content));

        assertThat(out.getContent()).isEqualTo(lines(timeString + " foo 42", timeString + " bar 23"));
    }

    @Test
    public void formatsNoEntries() {
        jsonStreamPrinter().run(new FixedContentSource(""));

        assertThat(out.getContent()).isEmpty();
    }

    @Test
    public void appliesFilter() {
        String content = "{\"name\": \"foo\", \"value\": 42}"
                + "{\"name\": \"bar\", \"value\": 23}"
                + "{\"name\": \"baz\", \"value\": 42}";
        jsonStreamPrinter(e -> e.value == 42).run(new FixedContentSource(content));

        assertThat(out.getContent()).isEqualTo(lines(timeString + " foo 42", timeString + " baz 42"));
    }

    @Test
    public void recordsError() {
        String content = "{\"name\": \"foo\", \"value\": 42}";
        String error = "End";
        jsonStreamPrinter().run(new FixedContentSource(content, errorMessage(error)));

        assertThat(err.getContent()).isEqualTo(lines(error));
    }

    @Test
    public void abortsOnMalformedJson() {
        String content = "{\"name\": \"foo\", \"value\": 42}" +
                        "BAD" +
                        "{\"name\": \"foo\", \"value\": 42}";
        jsonStreamPrinter().run(new FixedContentSource(content));

        assertThat(out.getContent()).isEqualTo(lines(timeString + " foo 42"));
        assertThat(err.getContent()).startsWith("Malformed JSON");
    }

    @Test
    public void reportsConnectionFailed() {
        String error = "failed";
        jsonStreamPrinter().run(new FailedConnectionSource(errorMessage(error)));
        assertThat(err.getContent()).isEqualTo(lines(error));
    }

    @Test
    public void printsHeader() {
        String header = "Header";
        String content = "{\"name\": \"foo\", \"value\": 42}";
        jsonStreamPrinter(header).run(new FixedContentSource(content));
        assertThat(out.getContent()).isEqualTo(lines(header, timeString + " foo 42"));
    }

    @Test
    public void omitsHeaderIfConnectionFailed() {
        String error = "failed";
        String header = "Header";
        jsonStreamPrinter(header).run(new FailedConnectionSource(errorMessage(error)));
        assertThat(err.getContent()).isEqualTo(lines(error));
    }

    private JsonStreamPrinter<TestEntry> jsonStreamPrinter(String header, Predicate<TestEntry> filter) {
        return new JsonStreamPrinter<>(
                clock, Ansi.OFF, TypeToken.get(TestEntry.class), filter, header, TestEntry::format);
    }

    private JsonStreamPrinter<TestEntry> jsonStreamPrinter(String header) {
        return jsonStreamPrinter(header, e -> true);
    }

    private JsonStreamPrinter<TestEntry> jsonStreamPrinter(Predicate<TestEntry> filter) {
        return jsonStreamPrinter(null, filter);
    }

    private JsonStreamPrinter<TestEntry> jsonStreamPrinter() {
        return jsonStreamPrinter(null, e -> true);
    }

    private static class TestEntry {
        public String name;
        public int value;

        public static String format(ZonedDateTime dateTime, TestEntry entry) {
            return String.format("%s %s %d", dateTime, entry.name, entry.value);
        }
    }
}

package com.github.ianparkinson.helog;

import com.github.ianparkinson.helog.testing.FixedContentSource;
import com.github.ianparkinson.helog.testing.StdErrExtension;
import com.github.ianparkinson.helog.testing.StdOutExtension;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.github.ianparkinson.helog.testing.TestStrings.lines;
import static com.google.common.truth.Truth.assertThat;

public final class JsonStreamPrinterTest {
    @RegisterExtension
    private final StdOutExtension out = new StdOutExtension();
    @RegisterExtension
    private final StdErrExtension err = new StdErrExtension();

    @Test
    public void formatsEntry() {
        JsonStreamPrinter<TestEntry> printer = jsonStreamPrinter("{\"name\": \"foo\", \"value\": 42}");
        printer.run();

        assertThat(out.getContent()).isEqualTo(lines("foo 42"));
    }

    @Test
    public void formatsMultipleEntries() {
        JsonStreamPrinter<TestEntry> printer = jsonStreamPrinter(
                "{\"name\": \"foo\", \"value\": 42}{\"name\": \"bar\", \"value\": 23}");
        printer.run();

        assertThat(out.getContent()).isEqualTo(lines("foo 42", "bar 23"));
    }

    @Test
    public void formatsNoEntries() {
        JsonStreamPrinter<TestEntry> printer = jsonStreamPrinter("");
        printer.run();

        assertThat(out.getContent()).isEmpty();
    }

    @Test
    public void recordsError() {
        JsonStreamPrinter<TestEntry> printer = jsonStreamPrinter(
                "{\"name\": \"foo\", \"value\": 42}",
                "End"
        );
        printer.run();

        assertThat(err.getContent()).isEqualTo(lines("End"));
    }

    @Test
    public void abortsOnMalformedJson() {
        JsonStreamPrinter<TestEntry> printer = jsonStreamPrinter(
                "{\"name\": \"foo\", \"value\": 42}" +
                        "BAD" +
                        "{\"name\": \"foo\", \"value\": 42}",
                null
        );
        printer.run();

        assertThat(out.getContent()).isEqualTo(lines("foo 42"));
        assertThat(err.getContent()).startsWith("Malformed JSON");
    }

    private static class TestEntry {
        public String name;
        public int value;

        public String format() {
            return String.format("%s %d", name, value);
        }
    }

    /**
     * Constructs a {@link JsonStreamPrinter} which will process given JSON content.
     */
    private JsonStreamPrinter<TestEntry> jsonStreamPrinter(String content) {
        return jsonStreamPrinter(content, null);
    }


    /**
     * Constructs a {@link JsonStreamPrinter} to process the given JSON content, with a simulated error.
     */
    private JsonStreamPrinter<TestEntry> jsonStreamPrinter(String content, String error) {
        return new JsonStreamPrinter<>(
                new FixedContentSource(content, error),
                TypeToken.get(TestEntry.class),
                TestEntry::format);
    }

}

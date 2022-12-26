package com.github.ianparkinson.helog;

import com.github.ianparkinson.helog.testing.FixedContentSource;
import com.github.ianparkinson.helog.testing.StdErrExtension;
import com.github.ianparkinson.helog.testing.StdOutExtension;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import picocli.CommandLine.Help.Ansi;

import java.util.function.Predicate;

import static com.github.ianparkinson.helog.ErrorMessage.errorMessage;
import static com.github.ianparkinson.helog.testing.TestStrings.lines;
import static com.google.common.truth.Truth.assertThat;

public final class JsonStreamPrinterTest {
    @RegisterExtension
    private final StdOutExtension out = new StdOutExtension();
    @RegisterExtension
    private final StdErrExtension err = new StdErrExtension();

    @Test
    public void formatsEntry() {
        String content = "{\"name\": \"foo\", \"value\": 42}";
        jsonStreamPrinter().run(new FixedContentSource(content));

        assertThat(out.getContent()).isEqualTo(lines("foo 42"));
    }

    @Test
    public void formatsMultipleEntries() {
        String content = "{\"name\": \"foo\", \"value\": 42}{\"name\": \"bar\", \"value\": 23}";
        jsonStreamPrinter().run(new FixedContentSource(content));

        assertThat(out.getContent()).isEqualTo(lines("foo 42", "bar 23"));
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

        assertThat(out.getContent()).isEqualTo(lines("foo 42", "baz 42"));
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

        assertThat(out.getContent()).isEqualTo(lines("foo 42"));
        assertThat(err.getContent()).startsWith("Malformed JSON");
    }

    private static JsonStreamPrinter<TestEntry> jsonStreamPrinter(Predicate<TestEntry> filter) {
        return new JsonStreamPrinter<>(
                Ansi.OFF, TypeToken.get(TestEntry.class), filter, TestEntry::format);
    }

    private static JsonStreamPrinter<TestEntry> jsonStreamPrinter() {
        return jsonStreamPrinter(e -> true);
    }

    private static class TestEntry {
        public String name;
        public int value;

        public String format() {
            return String.format("%s %d", name, value);
        }
    }
}

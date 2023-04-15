package com.github.ianparkinson.helog.app;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

final class JsonRendererTest {

    private static final ZonedDateTime DATE_TIME = ZonedDateTime.parse("2023-01-28T13:00Z");

    @Test
    void parsesAndFormats() {
        JsonRenderer<TestEntry> renderer =
                new JsonRenderer<>(TypeToken.get(TestEntry.class), entry -> true, TestEntry::format);
        assertThat(renderer.render(DATE_TIME, "{\"name\": \"foo\", \"value\": 42}"))
                .isEqualTo("2023-01-28T13:00Z foo 42");
    }

    @Test
    void filters() {
        JsonRenderer<TestEntry> renderer =
                new JsonRenderer<>(TypeToken.get(TestEntry.class), (entry) -> entry.value == 42, TestEntry::format);
        assertThat(renderer.render(DATE_TIME, "{\"name\": \"foo\", \"value\": 42}")).isNotNull();
        assertThat(renderer.render(DATE_TIME, "{\"name\": \"foo\", \"value\": 43}")).isNull();
    }

    @Test
    void jsonSyntaxException() {
        JsonRenderer<TestEntry> renderer =
                new JsonRenderer<>(TypeToken.get(TestEntry.class), entry -> true, TestEntry::format);
        assertThrows(JsonSyntaxException.class, () -> renderer.render(DATE_TIME, "this is not a JSON string"));
    }

    private static final class TestEntry {
        public String name;
        public int value;

        public static String format(ZonedDateTime dateTime, TestEntry entry) {
            return String.format("%s %s %d", dateTime, entry.name, entry.value);
        }
    }
}

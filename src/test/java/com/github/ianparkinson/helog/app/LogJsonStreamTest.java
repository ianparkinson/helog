package com.github.ianparkinson.helog.app;

import com.github.ianparkinson.helog.app.LogJsonStream.LogEntry;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LogJsonStreamTest {

    private static final String DATE_TIME_STRING = "2023-01-28T13:00:00.000Z";
    private static final ZonedDateTime DATE_TIME = ZonedDateTime.parse(DATE_TIME_STRING);

    private final LogJsonStream logJsonStream = new LogJsonStream();

    @Test
    void device_numericIdMatches() {
        LogEntry entry = new LogEntry();
        entry.type = "dev";
        entry.name = "n";
        entry.id = "42";
        assertThat(logJsonStream.device("42").test(entry)).isTrue();
    }

    @Test
    void device_nameMatches() {
        LogEntry entry = new LogEntry();
        entry.type = "dev";
        entry.name = "n";
        entry.id = "42";
        assertThat(logJsonStream.device("n").test(entry)).isTrue();
    }

    @Test
    void device_noMatch() {
        LogEntry entry = new LogEntry();
        entry.type = "dev";
        entry.name = "n";
        entry.id = "42";
        assertThat(logJsonStream.device("23").test(entry)).isFalse();
    }

    @Test
    void device_appWithMatchingId() {
        LogEntry entry = new LogEntry();
        entry.type = "app";
        entry.name = "n";
        entry.id = "42";
        assertThat(logJsonStream.device("42").test(entry)).isFalse();
    }

    @Test
    void device_appWithMatchingName() {
        LogEntry entry = new LogEntry();
        entry.type = "app";
        entry.name = "n";
        entry.id = "42";
        assertThat(logJsonStream.device("n").test(entry)).isFalse();
    }

    @Test
    void app_numericIdMatches() {
        LogEntry entry = new LogEntry();
        entry.type = "app";
        entry.name = "n";
        entry.id = "42";
        assertThat(logJsonStream.app("42").test(entry)).isTrue();
    }

    @Test
    void app_nameMatches() {
        LogEntry entry = new LogEntry();
        entry.type = "app";
        entry.name = "n";
        entry.id = "42";
        assertThat(logJsonStream.app("n").test(entry)).isTrue();
    }

    @Test
    void app_noMatch() {
        LogEntry entry = new LogEntry();
        entry.type = "app";
        entry.name = "n";
        entry.id = "42";
        assertThat(logJsonStream.app("23").test(entry)).isFalse();
    }

    @Test
    void app_deviceWithMatchingId() {
        LogEntry entry = new LogEntry();
        entry.type = "dev";
        entry.name = "n";
        entry.id = "42";
        assertThat(logJsonStream.app("42").test(entry)).isFalse();
    }

    @Test
    void app_deviceWithMatchingName() {
        LogEntry entry = new LogEntry();
        entry.type = "dev";
        entry.name = "n";
        entry.id = "42";
        assertThat(logJsonStream.app("n").test(entry)).isFalse();
    }

    @Test
    void eventName_notSupported() {
        assertThrows(UnsupportedOperationException.class, () -> logJsonStream.eventName("foo"));
    }

    @Test
    void logLevel_matches() {
        LogEntry entry = new LogEntry();
        entry.level = "debug";
        assertThat(logJsonStream.logLevel("debug").test(entry)).isTrue();
    }

    @Test
    void logLevel_noMatch() {
        LogEntry entry = new LogEntry();
        entry.level = "error";
        assertThat(logJsonStream.logLevel("debug").test(entry)).isFalse();
    }

    @Test
    void formatter() {
        LogEntry entry = new LogEntry();
        entry.name = "n";
        entry.msg = "m";
        entry.id = "i";
        entry.time = "ti";
        entry.type = "ty";
        entry.level = "l";

        assertThat(logJsonStream.formatter().format(DATE_TIME, entry)).isEqualTo("ti l      ty i n  m");
    }

    @Test
    void csvFormatter() {
        LogEntry entry = new LogEntry();
        entry.name = "n";
        entry.msg = "m";
        entry.id = "i";
        entry.time = "ti";
        entry.type = "ty";
        entry.level = "l";

        assertThat(logJsonStream.csvFormatter().format(DATE_TIME, entry))
                .containsExactly(DATE_TIME_STRING, "n", "m", "i", "ti", "ty", "l").inOrder();
    }
}
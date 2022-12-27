package com.github.ianparkinson.helog;

import com.github.ianparkinson.helog.LogJsonStream.LogEntry;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

class LogJsonStreamTest {

    private final LogJsonStream logJsonStream = new LogJsonStream();

    @Test
    void device_numericIdMatches() {
        LogEntry entry = new LogEntry();
        entry.name = "n";
        entry.id = "42";
        assertThat(logJsonStream.device("42").test(entry)).isTrue();
    }

    @Test
    void device_nameMatches() {
        LogEntry entry = new LogEntry();
        entry.name = "n";
        entry.id = "42";
        assertThat(logJsonStream.device("n").test(entry)).isTrue();
    }

    @Test
    void device_noMatch() {
        LogEntry entry = new LogEntry();
        entry.name = "n";
        entry.id = "42";
        assertThat(logJsonStream.device("23").test(entry)).isFalse();
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

        assertThat(logJsonStream.formatter().apply(entry)).isEqualTo("ti  ty  l     i:n  m");
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

        assertThat(logJsonStream.csvFormatter().apply(entry))
                .containsExactly("n", "m", "i", "ti", "ty", "l").inOrder();
    }
}
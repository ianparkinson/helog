package com.github.ianparkinson.helog;

import com.github.ianparkinson.helog.LogJsonStream.LogEntry;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

class LogJsonStreamTest {

    private final LogJsonStream logJsonStream = new LogJsonStream();

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
}
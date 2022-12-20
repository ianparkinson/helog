package com.github.ianparkinson.helog;

import com.github.ianparkinson.helog.EventsJsonStream.EventEntry;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

class EventsJsonStreamTest {
    private final EventsJsonStream eventsJsonStream = new EventsJsonStream();

    @Test
    void device_numericIdMatches() {
        EventEntry entry = new EventEntry();
        entry.displayName = "dn";
        entry.deviceId = 42;
        assertThat(eventsJsonStream.device("42").test(entry)).isTrue();
    }

    @Test
    void device_nameMatches() {
        EventEntry entry = new EventEntry();
        entry.displayName = "dn";
        entry.deviceId = 42;
        assertThat(eventsJsonStream.device("dn").test(entry)).isTrue();
    }

    @Test
    void device_noMatch() {
        EventEntry entry = new EventEntry();
        entry.displayName = "dn";
        entry.deviceId = 42;
        assertThat(eventsJsonStream.device("23").test(entry)).isFalse();
    }

    @Test
    void formatter() {
        EventEntry entry = new EventEntry();
        entry.source = "s";
        entry.name = "n";
        entry.displayName = "dn";
        entry.value = "v";
        entry.type = "t";
        entry.unit = "u";
        entry.deviceId = 1;
        entry.hubId = 2;
        entry.installedAppId = 3;
        entry.descriptionText= "dt";

        assertThat(eventsJsonStream.formatter().apply(entry)).isEqualTo("   1:dn n v u dt");
    }
}
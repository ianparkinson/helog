package com.github.ianparkinson.helog;

import com.github.ianparkinson.helog.EventsJsonStream.EventEntry;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

class EventsJsonStreamTest {
    private final EventsJsonStream eventsJsonStream = new EventsJsonStream();

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
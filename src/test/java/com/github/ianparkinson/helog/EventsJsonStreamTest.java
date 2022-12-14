package com.github.ianparkinson.helog;

import com.github.ianparkinson.helog.EventsJsonStream.EventEntry;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

class EventsJsonStreamTest {
    private final EventsJsonStream eventsJsonStream = new EventsJsonStream();

    @Test
    void device_numericIdMatches() {
        EventEntry entry = new EventEntry();
        entry.source = "DEVICE";
        entry.displayName = "dn";
        entry.deviceId = "42";
        assertThat(eventsJsonStream.device("42").test(entry)).isTrue();
    }

    @Test
    void device_nameMatches() {
        EventEntry entry = new EventEntry();
        entry.source = "DEVICE";
        entry.displayName = "dn";
        entry.deviceId = "42";
        assertThat(eventsJsonStream.device("dn").test(entry)).isTrue();
    }

    @Test
    void device_noMatch() {
        EventEntry entry = new EventEntry();
        entry.source = "DEVICE";
        entry.displayName = "dn";
        entry.deviceId = "42";
        assertThat(eventsJsonStream.device("23").test(entry)).isFalse();
    }

    @Test
    void device_appWithMatchingId() {
        EventEntry entry = new EventEntry();
        entry.source = "APP";
        entry.displayName = "dn";
        entry.deviceId = "42";
        assertThat(eventsJsonStream.device("42").test(entry)).isFalse();
    }

    @Test
    void device_appWithMatchingDisplayName() {
        EventEntry entry = new EventEntry();
        entry.source = "APP";
        entry.displayName = "dn";
        entry.deviceId = "42";
        assertThat(eventsJsonStream.device("dn").test(entry)).isFalse();
    }

    @Test
    void app_numericIdMatches() {
        EventEntry entry = new EventEntry();
        entry.source = "APP";
        entry.installedAppId = "42";
        assertThat(eventsJsonStream.app("42").test(entry)).isTrue();
    }

    @Test
    void app_noMatch() {
        EventEntry entry = new EventEntry();
        entry.source = "APP";
        entry.installedAppId = "42";
        assertThat(eventsJsonStream.app("23").test(entry)).isFalse();
    }

    @Test
    void app_deviceWithMatchingId() {
        EventEntry entry = new EventEntry();
        entry.source = "DEVICE";
        entry.displayName = "dn";
        entry.deviceId = "42";
        entry.installedAppId = "42";
        assertThat(eventsJsonStream.app("42").test(entry)).isFalse();
    }

    @Test
    void app_deviceWithMatchingDisplayName() {
        EventEntry entry = new EventEntry();
        entry.source = "DEVICE";
        entry.displayName = "dn";
        assertThat(eventsJsonStream.app("dn").test(entry)).isFalse();
    }

    @Test
    void formatterDevice() {
        EventEntry entry = new EventEntry();
        entry.source = "DEVICE";
        entry.name = "n";
        entry.displayName = "dn";
        entry.value = "v";
        entry.type = "t";
        entry.unit = "u";
        entry.deviceId = "1";
        entry.hubId = "2";
        entry.installedAppId = "0";
        entry.descriptionText= "dt";

        assertThat(eventsJsonStream.formatter().apply(entry)).isEqualTo("DEVICE 1 dn: n v u dt");
    }

    @Test
    void formatterApp() {
        EventEntry entry = new EventEntry();
        entry.source = "APP";
        entry.name = "n";
        entry.displayName = "dn";
        entry.value = "v";
        entry.type = "t";
        entry.unit = "u";
        entry.deviceId = "0";
        entry.hubId = "2";
        entry.installedAppId = "3";
        entry.descriptionText= "dt";

        assertThat(eventsJsonStream.formatter().apply(entry)).isEqualTo("APP    3 dn: n v u dt");
    }

    @Test
    void formatterAppUnknownSource() {
        EventEntry entry = new EventEntry();
        entry.source = "???";
        entry.name = "n";
        entry.displayName = "dn";
        entry.value = "v";
        entry.type = "t";
        entry.unit = "u";
        entry.deviceId = "1";
        entry.hubId = "2";
        entry.installedAppId = "3";
        entry.descriptionText= "dt";

        assertThat(eventsJsonStream.formatter().apply(entry)).isEqualTo("???    1 3 dn: n v u dt");
    }

    @Test
    void csvFormatter() {
        EventEntry entry = new EventEntry();
        entry.source = "s";
        entry.name = "n";
        entry.displayName = "dn";
        entry.value = "v";
        entry.type = "t";
        entry.unit = "u";
        entry.deviceId = "1";
        entry.hubId = "2";
        entry.installedAppId = "3";
        entry.descriptionText= "dt";

        assertThat(eventsJsonStream.csvFormatter().apply(entry)).containsExactly(
                "s", "n", "dn", "v", "t", "u", "1", "2", "3", "dt"
        ).inOrder();
    }
}
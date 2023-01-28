package com.github.ianparkinson.helog.app;

import com.github.ianparkinson.helog.app.EventsJsonStream.EventEntry;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EventsJsonStreamTest {

    private static final String DATE_TIME_STRING = "2023-01-28T13:00:00.000Z";
    private static final ZonedDateTime DATE_TIME = ZonedDateTime.parse(DATE_TIME_STRING);

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
    void eventName_matches() {
        EventEntry entry = new EventEntry();
        entry.name = "name";
        assertThat(eventsJsonStream.eventName("name").test(entry)).isTrue();
    }

    @Test
    void eventName_noMatch() {
        EventEntry entry = new EventEntry();
        entry.name = "name";
        assertThat(eventsJsonStream.eventName("other").test(entry)).isFalse();
    }

    @Test
    void logLevel_notSupported() {
        assertThrows(UnsupportedOperationException.class, () -> eventsJsonStream.logLevel("foo"));
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

        assertThat(eventsJsonStream.formatter().format(DATE_TIME, entry)).isEqualTo("DEVICE 1 dn: n v u dt");
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

        assertThat(eventsJsonStream.formatter().format(DATE_TIME, entry)).isEqualTo("APP    3 dn: n v u dt");
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

        assertThat(eventsJsonStream.formatter().format(DATE_TIME, entry)).isEqualTo("???    1 3 dn: n v u dt");
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

        assertThat(eventsJsonStream.csvFormatter().format(DATE_TIME, entry)).containsExactly(
                DATE_TIME_STRING, "s", "n", "dn", "v", "t", "u", "1", "2", "3", "dt"
        ).inOrder();
    }
}
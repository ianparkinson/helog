package com.github.ianparkinson.helog.util;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

import static com.github.ianparkinson.helog.util.DateTimeFormatters.ISO_OFFSET_DATE_TIME_MILLIS;
import static com.google.common.truth.Truth.assertThat;

public class DateTimeFormattersTest {
    @Test
    public void isoOffsetDateTimeMillis_zonedDateTimeUtc() {
        ZonedDateTime dateTime = ZonedDateTime.parse("2020-01-02T03:04:05.678Z");
        assertThat(ISO_OFFSET_DATE_TIME_MILLIS.format(dateTime))
                .isEqualTo("2020-01-02T03:04:05.678Z");
    }

    @Test
    public void isoOffsetDateTimeMillis_zonedDateTimeAfternoonUtc() {
        ZonedDateTime dateTime = ZonedDateTime.parse("2020-01-02T13:04:05.678Z");
        assertThat(ISO_OFFSET_DATE_TIME_MILLIS.format(dateTime))
                .isEqualTo("2020-01-02T13:04:05.678Z");
    }

    @Test
    public void isoOffsetDateTimeMillis_zonedDateTimeUtcZeroMillis() {
        ZonedDateTime dateTime = ZonedDateTime.parse("2020-01-02T03:04:05Z");
        assertThat(ISO_OFFSET_DATE_TIME_MILLIS.format(dateTime))
                .isEqualTo("2020-01-02T03:04:05.000Z");
    }

    @Test
    public void isoOffsetDateTimeMillis_zonedDateTimeUtcNanosTruncated() {
        ZonedDateTime dateTime = ZonedDateTime.parse("2020-01-02T03:04:05.678012Z");
        assertThat(ISO_OFFSET_DATE_TIME_MILLIS.format(dateTime))
                .isEqualTo("2020-01-02T03:04:05.678Z");
    }

    @Test
    public void isoOffsetDateTimeMillis_offsetDateTimeUtc() {
        OffsetDateTime dateTime = OffsetDateTime.parse("2020-01-02T03:04:05.678Z");
        assertThat(ISO_OFFSET_DATE_TIME_MILLIS.format(dateTime))
                .isEqualTo("2020-01-02T03:04:05.678Z");
    }

    @Test
    public void isoOffsetDateTimeMillis_offsetDateTimeAfternoonUtc() {
        OffsetDateTime dateTime = OffsetDateTime.parse("2020-01-02T13:04:05.678Z");
        assertThat(ISO_OFFSET_DATE_TIME_MILLIS.format(dateTime))
                .isEqualTo("2020-01-02T13:04:05.678Z");
    }

    @Test
    public void isoOffsetDateTimeMillis_zonedDateTimeWithZone() {
        ZonedDateTime dateTime = ZonedDateTime.parse("2020-01-02T03:04:05.678+01:00[Europe/Paris]");
        assertThat(ISO_OFFSET_DATE_TIME_MILLIS.format(dateTime))
                .isEqualTo("2020-01-02T03:04:05.678+01:00");
    }

    @Test
    public void isoOffsetDateTimeMillis_zonedDateTimeWithZoneZeroMillis() {
        ZonedDateTime dateTime = ZonedDateTime.parse("2020-01-02T03:04:05+01:00[Europe/Paris]");
        assertThat(ISO_OFFSET_DATE_TIME_MILLIS.format(dateTime))
                .isEqualTo("2020-01-02T03:04:05.000+01:00");
    }

    @Test
    public void isoOffsetDateTimeMillis_zonedDateTimeWithZoneNanosTruncated() {
        ZonedDateTime dateTime = ZonedDateTime.parse("2020-01-02T03:04:05.678012+01:00[Europe/Paris]");
        assertThat(ISO_OFFSET_DATE_TIME_MILLIS.format(dateTime))
                .isEqualTo("2020-01-02T03:04:05.678+01:00");
    }

    @Test
    public void isoOffsetDateTimeMillis_offsetDateTimeWithZone() {
        OffsetDateTime dateTime = OffsetDateTime.parse("2020-01-02T03:04:05.678+01:00");
        assertThat(ISO_OFFSET_DATE_TIME_MILLIS.format(dateTime))
                .isEqualTo("2020-01-02T03:04:05.678+01:00");
    }
}

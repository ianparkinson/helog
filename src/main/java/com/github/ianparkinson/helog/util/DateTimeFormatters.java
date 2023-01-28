package com.github.ianparkinson.helog.util;

import java.time.format.DateTimeFormatter;

public final class DateTimeFormatters {
    private DateTimeFormatters() {}

    /**
     * ISO date-time formatter that includes the millis field and an offset. Similar to {@link
     * DateTimeFormatter#ISO_OFFSET_DATE_TIME} but always outputs to milliseconds precision.
     */
    public static DateTimeFormatter ISO_OFFSET_DATE_TIME_MILLIS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
}

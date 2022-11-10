package com.github.ianparkinson.helog;

import com.google.gson.reflect.TypeToken;

import java.util.function.Function;

import static com.github.ianparkinson.helog.Strings.emptyIfNull;

/**
 * {@link JsonStream} encapsulating the Hubitat Elevation's Log stream
 */
public final class LogJsonStream implements JsonStream<LogJsonStream.LogEntry> {

    @Override
    public String path() {
        return "logsocket";
    }

    @Override
    public TypeToken<LogEntry> type() {
        return TypeToken.get(LogEntry.class);
    }

    @Override
    public Function<LogEntry, String> formatter() {
        return LogJsonStream::format;
    }

    private static String format(LogEntry entry) {
        return String.format("%s  %s  %-5s %s:%s  %s",
                emptyIfNull(entry.time),
                emptyIfNull(entry.type),
                emptyIfNull(entry.level),
                emptyIfNull(entry.id),
                emptyIfNull(entry.name),
                emptyIfNull(entry.msg));
    }

    /**
     * Represents an entry in the Hubitat Log.
     *
     * <p>Sample content:
     * {@code {"name":"Christmas Tree","msg":"setSysinfo: [led:off]","id":34,"time":"2022-11-05 16:25:52.729","type":"dev","level":"info"}}
     */
    public static class LogEntry {
        public String name;
        public String msg;
        public String id;
        public String time;
        public String type;
        public String level;
    }
}
package com.github.ianparkinson.helog.app;

import com.google.gson.reflect.TypeToken;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static com.github.ianparkinson.helog.util.Strings.emptyIfNull;
import static java.util.Arrays.asList;

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
    public Predicate<LogEntry> device(String device) {
        return entry -> "dev".equalsIgnoreCase(entry.type) &&
                (Objects.equals(entry.id, device) || Objects.equals(entry.name, device));
    }

    @Override
    public Predicate<LogEntry> app(String app) {
        return entry -> "app".equalsIgnoreCase(entry.type) &&
                (Objects.equals(entry.id, app) || Objects.equals(entry.name, app));
    }

    @Override
    public Predicate<LogEntry> eventName(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Predicate<LogEntry> logLevel(String level) {
        return entry -> Objects.equals(entry.level, level);
    }

    @Override
    public JsonStreamFormatter<LogEntry, String> formatter() {
        return LogJsonStream::format;
    }

    private static String format(ZonedDateTime dateTime, LogEntry entry) {
        return String.format("%s %-5s  %s %s %s  %s",
                emptyIfNull(entry.time),
                emptyIfNull(entry.level),
                emptyIfNull(entry.type),
                emptyIfNull(entry.id),
                emptyIfNull(entry.name),
                emptyIfNull(entry.msg));
    }

    @Override
    public List<String> csvHeader() {
        return asList("name", "msg", "id", "time", "type", "level");
    }

    @Override
    public JsonStreamFormatter<LogEntry, List<String>> csvFormatter() {
        return (dateTime, entry) -> asList(
                emptyIfNull(entry.name),
                emptyIfNull(entry.msg),
                emptyIfNull(entry.id),
                emptyIfNull(entry.time),
                emptyIfNull(entry.type),
                emptyIfNull(entry.level));
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
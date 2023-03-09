package com.github.ianparkinson.helog.app;

import com.google.gson.reflect.TypeToken;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.ianparkinson.helog.util.DateTimeFormatters.ISO_OFFSET_DATE_TIME_MILLIS;
import static com.github.ianparkinson.helog.util.Strings.emptyIfNull;
import static java.util.Arrays.asList;

/**
 * {@link JsonStream} encapsulating the Hubitat Elevation's Events stream
 */
public final class EventsJsonStream implements JsonStream<EventsJsonStream.EventEntry> {
    @Override
    public String path() {
        return "eventsocket";
    }

    @Override
    public TypeToken<EventEntry> type() {
        return TypeToken.get(EventEntry.class);
    }

    @Override
    public Predicate<EventEntry> device(String device) {
        return entry -> "DEVICE".equals(entry.source) &&
                (Objects.equals(entry.deviceId, device) || Objects.equals(entry.displayName, device));
    }

    @Override
    public Predicate<EventEntry> app(String app) {
        return entry -> "APP".equals(entry.source) && Objects.equals(entry.installedAppId, app);
    }

    @Override
    public Predicate<EventEntry> eventName(String name) {
        return entry -> Objects.equals(entry.name, name);
    }

    @Override
    public Predicate<EventEntry> logLevel(String level) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JsonStreamFormatter<EventEntry, String> formatter() {
        return EventsJsonStream::format;
    }

    private static String format(ZonedDateTime dateTime, EventEntry entry) {
        List<String> prefixParts = Stream.of(
                ISO_OFFSET_DATE_TIME_MILLIS.format(dateTime),
                String.format("%-6s", emptyIfNull(entry.source)),
                emptyIfNullOrZero(entry.deviceId),
                emptyIfNullOrZero(entry.installedAppId),
                emptyIfNull(entry.displayName))
                .filter(v -> !v.isEmpty())
                .collect(Collectors.toList());
        List<String> suffixParts = Stream.of(
                emptyIfNull(entry.name),
                emptyIfNull(entry.value),
                emptyIfNull(entry.unit),
                emptyIfNull(entry.descriptionText))
                .filter(v -> !v.isEmpty())
                .collect(Collectors.toList());
        return String.join(" ", prefixParts) + ": " + String.join(" ", suffixParts);
    }

    private static String emptyIfNullOrZero(String id) {
        id = emptyIfNull(id);
        return id.trim().equals("0") ? "" : id;
    }

    @Override
    public List<String> csvHeader() {
        return asList(
                "localTime",
                "source",
                "name",
                "displayName",
                "value",
                "type",
                "unit",
                "deviceId",
                "hubId",
                "installedAppId",
                "descriptionText");
    }

    @Override
    public JsonStreamFormatter<EventEntry, List<String>> csvFormatter() {
        return (zonedDateTime, entry) -> asList(
                ISO_OFFSET_DATE_TIME_MILLIS.format(zonedDateTime),
                emptyIfNull(entry.source),
                emptyIfNull(entry.name),
                emptyIfNull(entry.displayName),
                emptyIfNull(entry.value),
                emptyIfNull(entry.type),
                emptyIfNull(entry.unit),
                emptyIfNull(entry.deviceId),
                emptyIfNull(entry.hubId),
                emptyIfNull(entry.installedAppId),
                emptyIfNull(entry.descriptionText));
    }

    /**
     * Represents an entry in the Event log.
     *
     * <p>Sample content:
     * {@code { "source":"DEVICE","name":"switch","displayName" : "Christmas Tree", "value" : "off", "type" : "digital", "unit":"null","deviceId":34,"hubId":0,"installedAppId":0,"descriptionText" : "null"}}
     */
    public static class EventEntry {
        public String source;
        public String name;
        public String displayName;
        public String value;
        public String type;
        public String unit;
        public String deviceId;
        public String hubId;
        public String installedAppId;
        public String descriptionText;
    }
}
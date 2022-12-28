package com.github.ianparkinson.helog;

import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.ianparkinson.helog.Strings.emptyIfNull;
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
        return entry -> Objects.equals(entry.deviceId, device)
                || Objects.equals(entry.displayName, device);
    }

    @Override
    public Function<EventEntry, String> formatter() {
        return entry -> String.format("%4s:%s %s %s %s %s",
                emptyIfNull(entry.deviceId),
                emptyIfNull(entry.displayName),
                emptyIfNull(entry.name),
                emptyIfNull(entry.value),
                emptyIfNull(entry.unit),
                emptyIfNull(entry.descriptionText));
    }

    @Override
    public List<String> csvHeader() {
        return asList(
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
    public Function<EventEntry, List<String>> csvFormatter() {
        return entry -> asList(
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
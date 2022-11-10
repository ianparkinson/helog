package com.github.ianparkinson.helog;

import com.google.gson.reflect.TypeToken;

import java.util.function.Function;

import static com.github.ianparkinson.helog.Strings.emptyIfNull;

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
    public Function<EventEntry, String> formatter() {
        return entry -> String.format("%4d:%s %s %s %s %s",
                entry.deviceId,
                emptyIfNull(entry.displayName),
                emptyIfNull(entry.name),
                emptyIfNull(entry.value),
                emptyIfNull(entry.unit),
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
        public int deviceId;
        public int hubId;
        public int installedAppId;
        public String descriptionText;
    }
}
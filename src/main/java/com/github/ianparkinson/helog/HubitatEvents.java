package com.github.ianparkinson.helog;

import com.google.gson.reflect.TypeToken;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.ParentCommand;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;

import static com.github.ianparkinson.helog.Strings.emptyIfNull;

/**
 * Connects to the Events websocket of a Hubitat Evolution, and spools events to stdout.
 */
@Command(
        name = "events",
        header = "Writes the live event stream to stdout.")
public final class HubitatEvents implements Callable<Integer> {
    @ParentCommand
    private Helog parentCommand;

    @Mixin
    private Options options;

    public Integer call() throws URISyntaxException {
        URI uri = new URI("ws://" + options.host + "/eventsocket");
        WebSocketSource source = new WebSocketSource(uri);

        if (parentCommand.raw) {
            RawPrinter printer = new RawPrinter(source);
            printer.run();
        } else {
            JsonStreamPrinter<EventEntry> printer = new JsonStreamPrinter<>(
                    source, TypeToken.get(EventEntry.class), HubitatEvents::format);
            printer.run();
        }
        return -1;
    }

    private static String format(EventEntry entry) {
        return String.format("%4d:%s %s %s %s %s",
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
    private static class EventEntry {
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
package com.github.ianparkinson.helog;

import com.google.gson.reflect.TypeToken;
import picocli.CommandLine;
import picocli.CommandLine.Mixin;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;

import static com.github.ianparkinson.helog.Strings.emptyIfNull;

/**
 * Connects to the Log websocket of a Hubitat Elevation, and spools events to stdout.
 */
@CommandLine.Command(
        name = "log",
        header = "Writes the live log stream to stdout.")
public final class HubitatLog implements Callable<Integer> {
    @CommandLine.ParentCommand
    private Helog parentCommand;

    @Mixin
    private Options options;

    public Integer call() throws URISyntaxException {
        URI uri = new URI("ws://" + options.host + "/logsocket");
        WebSocketSource source = new WebSocketSource(uri);

        if (parentCommand.raw) {
            RawPrinter printer = new RawPrinter(source);
            printer.run();
        } else {
            JsonStreamPrinter<LogEntry> printer = new JsonStreamPrinter<>(
                    source, TypeToken.get(LogEntry.class), HubitatLog::format);
            printer.run();
        }
        return -1;
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
    private static class LogEntry {
        public String name;
        public String msg;
        public String id;
        public String time;
        public String type;
        public String level;
    }
}
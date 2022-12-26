package com.github.ianparkinson.helog;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import picocli.CommandLine.Help.Ansi;

import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.ianparkinson.helog.ErrorMessage.errorMessage;

/**
 * Reads a stream of events in JSON format, and writes them to stdout in a human-readable format.
 *
 * <p> Most of the time, each event from the Hubitat Elevation is received in a single call to {@link
 * java.net.http.WebSocket.Listener#onText}. However, occasionally an event can be split across multiple calls to
 * {@code onText} - to handle this, {@link WebSocketSource} concatenates all the received text into a single stream and
 * {@link JsonStreamPrinter} takes care of separating out the individual JSON entries.
 *
 * <p> The stream is expected to remain open until the process is killed, and this object's {@link #run(Source)} method
 * will block until either the process is killed or the stream closes in error.
 *
 * @param <T> The type used to represent entries in the JSON stream.
 */
public final class JsonStreamPrinter<T> {
    public static final Gson gson = new Gson();

    private final Ansi ansi;
    private final TypeToken<T> jsonTypeToken;
    private final Predicate<T> filter;
    private final Function<T, String> formatter;

    /**
     * @param ansi          Colorizes output.
     * @param jsonTypeToken Token for the type used to represent entries in the JSON stream. Passed to Gson to parse
     *                      the streamed JSON.
     * @param filter        Predicate selecting which entries to log.
     * @param formatter     Transforms the result of parsing JSON to a human-readable String.
     */
    public JsonStreamPrinter(
            Ansi ansi,
            TypeToken<T> jsonTypeToken,
            Predicate<T> filter,
            Function<T, String> formatter) {
        this.ansi = ansi;
        this.jsonTypeToken = jsonTypeToken;
        this.filter = filter;
        this.formatter = formatter;
    }

    /**
     * The main loop.
     *
     * <p> Connects to the stream and then blocks, reading entries from the stream one-by-one. Each entry is formatted
     * and written to stdout. Usually runs until the process is killed, but will complete if the stream fails with an
     * error.
     */
    public void run(Source source) {
        Source.Connection connection = source.connect();
        JsonReader jsonReader = gson.newJsonReader(connection.getReader());
        while (true) {
            try {
                T entry = gson.fromJson(jsonReader, jsonTypeToken);
                if (entry != null) {
                    if (filter.test(entry)) {
                        System.out.println(formatter.apply(entry));
                    }
                } else if (connection.getError() != null) {
                    connection.getError().writeToStderr(ansi);
                    return;
                } else {
                    errorMessage("Stream closed").writeToStderr(ansi);
                    return;
                }
            } catch (JsonSyntaxException e) {
                if (connection.getError() != null) {
                    connection.getError().writeToStderr(ansi);
                } else {
                    errorMessage("Malformed JSON", "%s", e.getMessage()).writeToStderr(ansi);
                }
                return;
            }
        }
    }
}

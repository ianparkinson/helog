package com.github.ianparkinson.helog.app;

import com.github.ianparkinson.helog.app.StreamPrinter.Renderer;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.time.ZonedDateTime;
import java.util.function.Predicate;

/**
 * Parses JSON strings, filters them, and formats them using {@link JsonStreamFormatter}.
 *
 * @param <T> Event type representing an entry in the stream.
 */
public class JsonRenderer<T> implements Renderer {
    public static final Gson gson = new Gson();

    private final TypeToken<T> jsonTypeToken;
    private final Predicate<T> filter;
    private final JsonStreamFormatter<T, String> formatter;

    public JsonRenderer(TypeToken<T> jsonTypeToken, Predicate<T> filter, JsonStreamFormatter<T, String> formatter) {
        this.jsonTypeToken = jsonTypeToken;
        this.filter = filter;
        this.formatter = formatter;
    }

    @Override
    public String render(ZonedDateTime dateTime, String text) {
        T entry = gson.fromJson(text, jsonTypeToken);
        if (entry != null && filter.test(entry)) {
            return formatter.format(dateTime, entry);
        } else {
            return null;
        }
    }
}

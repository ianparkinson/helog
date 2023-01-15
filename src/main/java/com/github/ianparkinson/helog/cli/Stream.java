package com.github.ianparkinson.helog.cli;

import com.github.ianparkinson.helog.app.EventsJsonStream;
import com.github.ianparkinson.helog.app.JsonStream;
import com.github.ianparkinson.helog.app.LogJsonStream;

/**
 * Streams which can be selected at the command line.
 */
public enum Stream {
    LOG(new LogJsonStream()),
    EVENTS(new EventsJsonStream());

    public final JsonStream<?> jsonStream;

    Stream(JsonStream<?> jsonStream) {
        this.jsonStream = jsonStream;
    }
}

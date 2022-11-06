package com.github.ianparkinson.helog.testing;

import com.github.ianparkinson.helog.Source;

import java.io.Reader;
import java.io.StringReader;

/**
 * Encapsulates, for testing, a canned response from the WebSocket server.
 */
public final class FixedContentSource implements Source {
    private final String content;
    private final String error;

    public FixedContentSource(String content, String error) {
        this.content = content;
        this.error = error;
    }

    @Override
    public Connection connect() {
        return new Connection(new StringReader(content), error);
    }

    private static class Connection implements Source.Connection {
        private final Reader reader;
        private final String error;

        private Connection(Reader reader, String error) {
            this.reader = reader;
            this.error = error;
        }

        @Override
        public Reader getReader() {
            return reader;
        }

        @Override
        public String getError() {
            return error;
        }
    }
}

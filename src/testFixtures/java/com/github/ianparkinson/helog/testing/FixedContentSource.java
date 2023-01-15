package com.github.ianparkinson.helog.testing;

import com.github.ianparkinson.helog.util.ErrorMessage;
import com.github.ianparkinson.helog.app.Source;

import java.io.Reader;
import java.io.StringReader;

/**
 * Encapsulates, for testing, a canned response from the WebSocket server.
 */
public final class FixedContentSource implements Source {
    private final String content;
    private final ErrorMessage error;

    public FixedContentSource(String content) {
        this.content = content;
        this.error = null;
    }

    public FixedContentSource(String content, ErrorMessage error) {
        this.content = content;
        this.error = error;
    }

    @Override
    public Connection connect() {
        return new Connection(new StringReader(content), error);
    }

    private static class Connection implements Source.Connection {
        private final Reader reader;
        private final ErrorMessage error;

        private Connection(Reader reader, ErrorMessage error) {
            this.reader = reader;
            this.error = error;
        }

        @Override
        public Reader getReader() {
            return reader;
        }

        @Override
        public ErrorMessage getError() {
            return error;
        }
    }
}

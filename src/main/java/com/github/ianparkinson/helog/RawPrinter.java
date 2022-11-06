package com.github.ianparkinson.helog;

import java.io.IOException;
import java.io.Reader;

/**
 * Writes text from a {@link Source} out to stdout.
 */
public final class RawPrinter {
    private final Source source;

    public RawPrinter(Source source) {
        this.source = source;
    }

    public void run() {
        Source.Connection connection = source.connect();

        char[] buffer = new char[1024];
        try (Reader reader = connection.getReader()) {
            while (true) {
                int count = reader.read(buffer);
                if (count >= 0) {
                    System.out.print(new String(buffer, 0, count));
                } else {
                    if (connection.getError() != null) {
                        System.err.println(connection.getError());
                    } else {
                        System.err.println("Stream closed");
                    }
                    return;
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}

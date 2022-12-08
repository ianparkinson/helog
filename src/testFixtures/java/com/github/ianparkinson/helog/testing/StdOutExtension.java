package com.github.ianparkinson.helog.testing;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Captures text sent to {@code System.out} for the duration of a test. Captured content can be retrieved using
 * {@link #getContent()}.
 */
public final class StdOutExtension implements BeforeEachCallback, AfterEachCallback {
    private PrintStream original;
    private final ByteArrayOutputStream stream = new ByteArrayOutputStream();

    @Override
    public void beforeEach(ExtensionContext context) {
        original = System.out;
        System.setOut(new PrintStream(stream, false, UTF_8));
    }

    @Override
    public void afterEach(ExtensionContext context) {
        System.setOut(original);
    }

    /**
     * Returns text captured since the start of the test.
     */
    public String getContent() {
        return stream.toString(UTF_8);
    }
}

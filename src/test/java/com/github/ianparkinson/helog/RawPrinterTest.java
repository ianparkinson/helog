package com.github.ianparkinson.helog;

import com.github.ianparkinson.helog.testing.FixedContentSource;
import com.github.ianparkinson.helog.testing.StdErrExtension;
import com.github.ianparkinson.helog.testing.StdOutExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.github.ianparkinson.helog.testing.TestStrings.lines;
import static com.google.common.truth.Truth.assertThat;

public final class RawPrinterTest {
    @RegisterExtension
    private final StdOutExtension out = new StdOutExtension();
    @RegisterExtension
    private final StdErrExtension err = new StdErrExtension();

    @Test
    public void spoolsText() {
        String content = "Test Content";
        RawPrinter printer = new RawPrinter(new FixedContentSource(content, null));
        printer.run();

        assertThat(out.getContent()).isEqualTo(content);
    }

    @Test
    public void recordsError() {
        String error = "Some error";
        RawPrinter printer = new RawPrinter(new FixedContentSource("", error));
        printer.run();

        assertThat(err.getContent()).isEqualTo(lines(error));
    }
}

package com.github.ianparkinson.helog.app;

import com.github.ianparkinson.helog.testing.FailedConnectionSource;
import com.github.ianparkinson.helog.testing.FixedContentSource;
import com.github.ianparkinson.helog.testing.StdErrExtension;
import com.github.ianparkinson.helog.testing.StdOutExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import picocli.CommandLine.Help.Ansi;

import static com.github.ianparkinson.helog.util.ErrorMessage.errorMessage;
import static com.github.ianparkinson.helog.testing.TestStrings.lines;
import static com.google.common.truth.Truth.assertThat;

public final class RawPrinterTest {
    @RegisterExtension
    private final StdOutExtension out = new StdOutExtension();
    @RegisterExtension
    private final StdErrExtension err = new StdErrExtension();

    private final RawPrinter rawPrinter = new RawPrinter(Ansi.OFF);

    @Test
    public void spoolsText() {
        String content = "Test Content";
        rawPrinter.run(new FixedContentSource(content));

        assertThat(out.getContent()).isEqualTo(content);
    }

    @Test
    public void recordsError() {
        String error = "Some error";
        rawPrinter.run(new FixedContentSource("", errorMessage(error)));

        assertThat(err.getContent()).isEqualTo(lines(error));
    }

    @Test
    public void reportsConnectionFailed() {
        String error = "failed";
        rawPrinter.run(new FailedConnectionSource(errorMessage(error)));
        assertThat(err.getContent()).isEqualTo(lines(error));
    }
}

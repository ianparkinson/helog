package com.github.ianparkinson.helog;

import com.github.ianparkinson.helog.testing.StdErrExtension;
import com.github.ianparkinson.helog.testing.StdOutExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.google.common.truth.Truth.assertThat;

public class HelogNoArgsTest {
    @RegisterExtension
    private final StdOutExtension out = new StdOutExtension();

    @RegisterExtension
    private final StdErrExtension err = new StdErrExtension();

    @Test
    public void writesUsageToStderr() {
        Helog.run();

        // Check that stderr starts with the Header, not with Picocli's error message. The content sent to stderr
        // will have newlines; replace those with spaces before testing against the header.
        assertThat(err.getContent().replaceAll("\\R", " ")).startsWith(Helog.HEADER);
    }

    @Test
    public void stdOutIsEmpty() {
        Helog.run();
        assertThat(out.getContent()).isEmpty();
    }

    @Test
    public void exitCode() {
        assertThat(Helog.run()).isEqualTo(2);
    }
}
package com.github.ianparkinson.helog.integration;

import com.github.ianparkinson.helog.testing.HelogCommand;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

/** Checks that the {@code helog} command emits the version string when requested. */
public class VersionTest {
    private final String expectedVersion = System.getProperty("helog.expected.version");

    @Test
    public void expectedVersionAvailable() {
        assertThat(expectedVersion).isNotEmpty();
    }

    @Test
    public void printsVersion() throws IOException, InterruptedException {
        HelogCommand.Result result = HelogCommand.run("--version");
        assertThat(result.stdOut).contains(expectedVersion);
        assertThat(result.exitCode).isEqualTo(0);
    }
}

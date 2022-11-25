package com.github.ianparkinson.helog;

import static com.google.common.truth.Truth.assertThat;

import com.github.ianparkinson.helog.testing.StdOutExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public final class HelogKofiTest {
    @RegisterExtension
    private final StdOutExtension out = new StdOutExtension();

    @Test
    public void writesKofiLink() {
        Helog.run("--kofi");
        assertThat(out.getContent()).contains("ko-fi.com");
    }

    @Test
    public void completesSuccessfully() {
        assertThat(Helog.run("--kofi")).isEqualTo(0);
    }
}

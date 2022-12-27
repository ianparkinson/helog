package com.github.ianparkinson.helog;

import com.google.common.truth.Truth;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public final class StringsTest {
    @Test
    public void emptyIfNull_notEmpty() {
        Truth.assertThat(Strings.emptyIfNull("not null")).isEqualTo("not null");
    }

    @Test
    public void emptyIfNull_null() {
        assertThat(Strings.emptyIfNull(null)).isEmpty();
    }

    @Test
    public void emptyIfNull_literallyNull() {
        assertThat(Strings.emptyIfNull("null")).isEmpty();
    }

    @Test
    public void emptyIfNull_caseInsensitive() {
        assertThat(Strings.emptyIfNull("NULL")).isEmpty();
    }

    @Test
    public void isHostPort_ipAddress() {
        assertThat(Strings.isHostPort("1.2.3.4")).isTrue();
    }

    @Test
    public void isHostPort_hostName() {
        assertThat(Strings.isHostPort("a.b.c")).isTrue();
    }

    @Test
    public void isHostPort_ipAddressWithPort() {
        assertThat(Strings.isHostPort("1.2.3.4:56")).isTrue();
    }

    @Test
    public void isHostPort_hostNameWithPort() {
        assertThat(Strings.isHostPort("a.b.c:12")).isTrue();
    }

    @Test
    public void isHostPort_uri() {
        assertThat(Strings.isHostPort("ws://example.com/")).isFalse();
    }

    @Test
    public void isHostPort_uriWithIpAddressAndPath() {
        assertThat(Strings.isHostPort("ws://1.2.3.4/logevents")).isFalse();
    }

    @Test
    public void csvLineNoEntries() {
        assertThat(Strings.csvLine(List.of())).isEqualTo("");
    }

    @Test
    public void csvLineOneEntry() {
        assertThat(Strings.csvLine(List.of("hello"))).isEqualTo("hello");
    }

    @Test
    public void csvLineTwoEntries() {
        assertThat(Strings.csvLine(List.of("hello", "world"))).isEqualTo("hello,world");
    }

    @Test
    public void csvLineEscapesSpecialCharacters() {
        assertThat(Strings.csvLine(List.of("tab\t", "newline\n", "return\r", "quote\"")))
                .isEqualTo("\"tab\t\",\"newline\n\",\"return\r\",\"quote\"\"\"");
    }

    @Test
    public void csvLineDoubleQuotesAtStart() {
        assertThat(Strings.csvLine(List.of("\"hello"))).isEqualTo("\"\"\"hello\"");
    }

    @Test
    public void csvLineDoubleQuotesAtEnd() {
        assertThat(Strings.csvLine(List.of("hello\""))).isEqualTo("\"hello\"\"\"");
    }

    @Test
    public void csvLineDoubleQuotesInMiddle() {
        assertThat(Strings.csvLine(List.of("hello\"world"))).isEqualTo("\"hello\"\"world\"");
    }
}

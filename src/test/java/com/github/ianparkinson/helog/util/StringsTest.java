package com.github.ianparkinson.helog.util;

import com.google.common.truth.Truth;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

final class StringsTest {
    @Test
    void emptyIfNull_notEmpty() {
        Truth.assertThat(Strings.emptyIfNull("not null")).isEqualTo("not null");
    }

    @Test
    void emptyIfNull_null() {
        assertThat(Strings.emptyIfNull(null)).isEmpty();
    }

    @Test
    void emptyIfNull_literallyNull() {
        assertThat(Strings.emptyIfNull("null")).isEmpty();
    }

    @Test
    void emptyIfNull_caseInsensitive() {
        assertThat(Strings.emptyIfNull("NULL")).isEmpty();
    }

    @Test
    void emptyIfNullOrZero() {
        assertThat(Strings.emptyIfNull("0")).isEqualTo("0");
    }

    @Test
    void emptyIfNullOrZero_notEmpty() {
        Truth.assertThat(Strings.emptyIfNullOrZero("not null")).isEqualTo("not null");
    }

    @Test
    void emptyIfNullOrZero_null() {
        assertThat(Strings.emptyIfNullOrZero(null)).isEmpty();
    }

    @Test
    void emptyIfNullOrZero_literallyNull() {
        assertThat(Strings.emptyIfNullOrZero("null")).isEmpty();
    }

    @Test
    void emptyIfNullOrZero_caseInsensitive() {
        assertThat(Strings.emptyIfNullOrZero("NULL")).isEmpty();
    }

    @Test
    void emptyIfNullOrZero_zero() {
        assertThat(Strings.emptyIfNullOrZero("0")).isEmpty();
    }

    @Test
    void emptyIfNullOrZero_number() {
        assertThat(Strings.emptyIfNullOrZero("1")).isEqualTo("1");
    }

    @Test
    void isHostPort_ipAddress() {
        assertThat(Strings.isHostPort("1.2.3.4")).isTrue();
    }

    @Test
    void isHostPort_hostName() {
        assertThat(Strings.isHostPort("a.b.c")).isTrue();
    }

    @Test
    void isHostPort_ipAddressWithPort() {
        assertThat(Strings.isHostPort("1.2.3.4:56")).isTrue();
    }

    @Test
    void isHostPort_hostNameWithPort() {
        assertThat(Strings.isHostPort("a.b.c:12")).isTrue();
    }

    @Test
    void isHostPort_uri() {
        assertThat(Strings.isHostPort("ws://example.com/")).isFalse();
    }

    @Test
    void isHostPort_uriWithIpAddressAndPath() {
        assertThat(Strings.isHostPort("ws://1.2.3.4/logevents")).isFalse();
    }

    @Test
    void isInteger_null() {
        assertThat(Strings.isInteger(null)).isFalse();
    }

    @Test
    void isInteger_int() {
        assertThat(Strings.isInteger("42")).isTrue();
    }

    @Test
    void isInteger_empty() {
        assertThat(Strings.isInteger("")).isFalse();
    }

    @Test
    void isInteger_alpha() {
        assertThat(Strings.isInteger("text")).isFalse();
    }

    @Test
    void csvLineNoEntries() {
        assertThat(Strings.csvLine(List.of())).isEqualTo("");
    }

    @Test
    void csvLineOneEntry() {
        assertThat(Strings.csvLine(List.of("hello"))).isEqualTo("hello");
    }

    @Test
    void csvLineTwoEntries() {
        assertThat(Strings.csvLine(List.of("hello", "world"))).isEqualTo("hello,world");
    }

    @Test
    void csvLineOneNull() {
        ArrayList<String> list = new ArrayList<>();
        list.add(null);
        assertThat(Strings.csvLine(list)).isEqualTo("");
    }

    @Test
    void csvLineToleratesNulls() {
        assertThat(Strings.csvLine(Arrays.asList("hello", null, "world"))).isEqualTo("hello,,world");
    }

    @Test
    void csvLineEscapesSpecialCharacters() {
        assertThat(Strings.csvLine(List.of("tab\t", "newline\n", "return\r", "quote\"")))
                .isEqualTo("\"tab\t\",\"newline\n\",\"return\r\",\"quote\"\"\"");
    }

    @Test
    void csvLineDoubleQuotesAtStart() {
        assertThat(Strings.csvLine(List.of("\"hello"))).isEqualTo("\"\"\"hello\"");
    }

    @Test
    void csvLineDoubleQuotesAtEnd() {
        assertThat(Strings.csvLine(List.of("hello\""))).isEqualTo("\"hello\"\"\"");
    }

    @Test
    void csvLineDoubleQuotesInMiddle() {
        assertThat(Strings.csvLine(List.of("hello\"world"))).isEqualTo("\"hello\"\"world\"");
    }
}

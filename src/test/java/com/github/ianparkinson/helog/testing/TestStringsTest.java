package com.github.ianparkinson.helog.testing;

import org.junit.jupiter.api.Test;

import static com.github.ianparkinson.helog.testing.TestStrings.splitLines;
import static com.google.common.truth.Truth.assertThat;

public class TestStringsTest {
    @Test
    public void splitLines_empty() {
        assertThat(splitLines("")).isEmpty();
    }

    @Test
    public void splitLines_singleEmptyLine() {
        assertThat(splitLines("\n")).containsExactly("");
    }

    @Test
    public void splitLines_singleLineWithText() {
        assertThat(splitLines("foo\n")).containsExactly("foo");
    }

    @Test
    public void splitLines_singleLineThenEmptyLine() {
        assertThat(splitLines("foo\n\n")).containsExactly("foo", "").inOrder();
    }

    @Test
    public void splitLines_unterminatedLine() {
        assertThat(splitLines("foo")).containsExactly("foo");
    }

    @Test
    public void splitLines_emptyThenUnterminated() {
        assertThat(splitLines("\nfoo")).containsExactly("", "foo").inOrder();
    }

    @Test
    public void splitLines_twoLinesUnterminated() {
        assertThat(splitLines("foo\nbar")).containsExactly("foo", "bar").inOrder();
    }

    @Test
    public void splitLines_twoLinesTerminated() {
        assertThat(splitLines("foo\nbar\n")).containsExactly("foo", "bar").inOrder();
    }

    @Test
    public void splitLines_emptyLineInMiddle() {
        assertThat(splitLines("foo\n\nbar")).containsExactly("foo", "", "bar").inOrder();
    }
}

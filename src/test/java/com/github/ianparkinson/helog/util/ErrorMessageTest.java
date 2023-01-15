package com.github.ianparkinson.helog.util;

import com.github.ianparkinson.helog.testing.StdErrExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import picocli.CommandLine.Help.Ansi;

import static com.github.ianparkinson.helog.util.ErrorMessage.errorMessage;
import static com.github.ianparkinson.helog.testing.TestStrings.lines;
import static com.google.common.truth.Truth.assertThat;

public class ErrorMessageTest {
    @RegisterExtension
    private final StdErrExtension err = new StdErrExtension();

    @Test
    public void toStringWithJustMessage() {
        String message = "testMessage";
        ErrorMessage errorMessage = errorMessage(message);
        assertThat(errorMessage.toString()).isEqualTo(message);
    }

    @Test
    public void toStringWithDetail() {
        ErrorMessage errorMessage = errorMessage("testMessage", "%d %s", 42, "foo");
        assertThat(errorMessage.toString()).isEqualTo("testMessage: 42 foo");
    }

    @Test
    public void toStringAnsiOffWithJustMessage() {
        String message = "testMessage";
        ErrorMessage errorMessage = errorMessage(message);
        assertThat(errorMessage.toString(Ansi.OFF)).isEqualTo(message);
    }

    @Test
    public void toStringAnsiOffWithDetail() {
        ErrorMessage errorMessage = errorMessage("testMessage", "%d %s", 42, "foo");
        assertThat(errorMessage.toString(Ansi.OFF)).isEqualTo("testMessage: 42 foo");
    }

    @Test
    public void toStringAnsiOnWithJustMessage() {
        ErrorMessage errorMessage = errorMessage("testMessage");
        String expected = Ansi.ON.string("@|red,bold testMessage|@");
        assertThat(errorMessage.toString(Ansi.ON)).isEqualTo(expected);
    }

    @Test
    public void toStringAnsiOnWithDetail() {
        ErrorMessage errorMessage = errorMessage("testMessage", "%d %s", 42, "foo");
        String expected = Ansi.ON.string("@|red,bold testMessage:|@ 42 foo");
        assertThat(errorMessage.toString(Ansi.ON)).isEqualTo(expected);
    }

    @Test
    public void writeToStderrAnsiOffWithJustMessage() {
        String message = "testMessage";
        errorMessage(message).writeToStderr(Ansi.OFF);
        assertThat(err.getContent()).isEqualTo(lines(message));
    }

    @Test
    public void writeToStderrAnsiOffWithDetail() {
        errorMessage("testMessage", "%d %s", 42, "foo").writeToStderr(Ansi.OFF);
        assertThat(err.getContent()).isEqualTo(lines("testMessage: 42 foo"));
    }

    @Test
    public void writeToStderrAnsiOnWithJustMessage() {
        errorMessage("testMessage").writeToStderr(Ansi.ON);
        String expected = Ansi.ON.string("@|red,bold testMessage|@");
        assertThat(err.getContent()).isEqualTo(lines(expected));
    }

    @Test
    public void writeToStderrAnsiOnWithDetail() {
        errorMessage("testMessage", "%d %s", 42, "foo").writeToStderr(Ansi.ON);
        String expected = Ansi.ON.string("@|red,bold testMessage:|@ 42 foo");
        assertThat(err.getContent()).isEqualTo(lines(expected));
    }
}

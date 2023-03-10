package com.github.ianparkinson.helog.integration;

import com.github.ianparkinson.helog.testing.HelogCommand;
import com.github.ianparkinson.helog.testing.WebSocketServerExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import static com.github.ianparkinson.helog.testing.TestStrings.dropDateTime;
import static com.github.ianparkinson.helog.testing.TestStrings.extractDateTime;
import static com.github.ianparkinson.helog.testing.TestStrings.splitLines;
import static com.google.common.truth.Truth.assertThat;

/**
 * Checks that that {@code helog} command successfully connects to a websockets server, and parses and formats
 * events.
 */
public class WritesLogsTest {
    @RegisterExtension
    private final WebSocketServerExtension webServer = new WebSocketServerExtension();

    @Test
    public void writesEvent() throws IOException, InterruptedException {
        webServer.content.add("{ \"source\":\"DEVICE\",\"name\":\"switch\",\"displayName\" : \"Christmas Tree\", " +
                "\"value\" : \"off\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":34,\"hubId\":0," +
                "\"installedAppId\":0,\"descriptionText\" : \"null\"}");
        HelogCommand.Result result = HelogCommand.run("events", webServer.getHostAndPort());

        List<String> lines = splitLines(result.stdOut);
        assertThat(lines).hasSize(1);
        assertThat(dropDateTime(lines.get(0))).isEqualTo(" DEVICE 34 Christmas Tree: switch off");
    }

    @Test
    public void eventTimestamp() throws IOException, InterruptedException {
        webServer.content.add("{ \"source\":\"DEVICE\",\"name\":\"switch\",\"displayName\" : \"Christmas Tree\", " +
                "\"value\" : \"off\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":34,\"hubId\":0," +
                "\"installedAppId\":0,\"descriptionText\" : \"null\"}");

        OffsetDateTime start = OffsetDateTime.now(ZoneId.systemDefault());
        HelogCommand.Result result = HelogCommand.run("events", webServer.getHostAndPort());
        OffsetDateTime end = OffsetDateTime.now(ZoneId.systemDefault());

        List<String> lines = splitLines(result.stdOut);
        OffsetDateTime reported = extractDateTime(lines.get(0));
        assertThat(reported).isAtLeast(start);
        assertThat(reported).isAtMost(end);
    }

    @Test
    public void writesLog() throws IOException, InterruptedException {
        webServer.content.add("{\"name\":\"Christmas Tree\",\"msg\":\"setSysinfo: led:off\",\"id\":34, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        HelogCommand.Result result = HelogCommand.run("log", webServer.getHostAndPort());

        List<String> lines = splitLines(result.stdOut);
        assertThat(lines).hasSize(1);
        assertThat(dropDateTime(lines.get(0))).isEqualTo(" info   dev 34 Christmas Tree  setSysinfo: led:off");
    }

    @Test
    public void logTimestamp() throws IOException, InterruptedException {
        webServer.content.add("{\"name\":\"Christmas Tree\",\"msg\":\"setSysinfo: led:off\",\"id\":34, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");

        OffsetDateTime start = OffsetDateTime.now(ZoneId.systemDefault());
        HelogCommand.Result result = HelogCommand.run("log", webServer.getHostAndPort());
        OffsetDateTime end = OffsetDateTime.now(ZoneId.systemDefault());

        List<String> lines = splitLines(result.stdOut);
        OffsetDateTime reported = extractDateTime(lines.get(0));
        assertThat(reported).isAtLeast(start);
        assertThat(reported).isAtMost(end);
    }

    @Test
    public void eventCommandFailsAtEndOfStream() throws IOException, InterruptedException {
        HelogCommand.Result result = HelogCommand.run("events", webServer.getHostAndPort());
        assertThat(result.exitCode).isEqualTo(1);
    }

    @Test
    public void logCommandFailsAtEndOfStream() throws IOException, InterruptedException {
        HelogCommand.Result result = HelogCommand.run("log", webServer.getHostAndPort());
        assertThat(result.exitCode).isEqualTo(1);
    }
}

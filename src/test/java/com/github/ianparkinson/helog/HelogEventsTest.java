package com.github.ianparkinson.helog;

import com.github.ianparkinson.helog.testing.StdErrExtension;
import com.github.ianparkinson.helog.testing.StdOutExtension;
import com.github.ianparkinson.helog.testing.WebSocketServerExtension;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.github.ianparkinson.helog.testing.TestStrings.lines;
import static com.google.common.truth.Truth.assertThat;

public final class HelogEventsTest {
    @RegisterExtension
    private final WebSocketServerExtension webServer = new WebSocketServerExtension();
    @RegisterExtension
    private final StdOutExtension out = new StdOutExtension();
    @RegisterExtension
    private final StdErrExtension err = new StdErrExtension();

    @Test
    public void connectsToEventSocket() throws InterruptedException {
        Helog.run("events", webServer.getHostAndPort());
        RecordedRequest request = webServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/eventsocket");
    }

    @Test
    public void writesFormattedJson() {
        webServer.content.add("{ \"source\":\"DEVICE\",\"name\":\"switch\",\"displayName\" : \"Christmas Tree\", " +
                "\"value\" : \"off\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":34,\"hubId\":0," +
                "\"installedAppId\":0,\"descriptionText\" : \"null\"}");
        Helog.run("events", webServer.getHostAndPort());
        assertThat(out.getContent()).isEqualTo(lines("  34:Christmas Tree switch off  "));
    }

    @Test
    public void toleratesSplitJson() {
        webServer.content.add("{ \"source\":\"DEVICE\",\"name\":\"switch\",\"displayName\" : \"Christmas Tree\", ");
        webServer.content.add("\"value\" : \"off\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":34,");
        webServer.content.add("\"hubId\":0,\"installedAppId\":0,\"descriptionText\" : \"null\"}");
        Helog.run("events", webServer.getHostAndPort());
        assertThat(out.getContent()).isEqualTo(lines("  34:Christmas Tree switch off  "));
    }

    @Test
    public void rawSpoolsExact() {
        webServer.content.add("abc");
        webServer.content.add("def");
        Helog.run("events", webServer.getHostAndPort(), "--raw");
        assertThat(out.getContent()).isEqualTo("abcdef");
    }

    @Test
    public void exitCode1() {
        assertThat(Helog.run("events", webServer.getHostAndPort())).isEqualTo(1);
    }
}

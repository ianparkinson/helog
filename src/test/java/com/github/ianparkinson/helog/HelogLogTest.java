package com.github.ianparkinson.helog;

import com.github.ianparkinson.helog.testing.StdErrExtension;
import com.github.ianparkinson.helog.testing.StdOutExtension;
import com.github.ianparkinson.helog.testing.WebSocketServerExtension;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.github.ianparkinson.helog.testing.TestStrings.lines;
import static com.google.common.truth.Truth.assertThat;

public final class HelogLogTest {
    @RegisterExtension
    private final WebSocketServerExtension webServer = new WebSocketServerExtension();
    @RegisterExtension
    private final StdOutExtension out = new StdOutExtension();
    @RegisterExtension
    private final StdErrExtension err = new StdErrExtension();

    @Test
    public void connectsToEventSocket() throws InterruptedException {
        Helog.run("log", webServer.getHostAndPort());
        RecordedRequest request = webServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/logsocket");
    }

    @Test
    public void writesFormattedJson() {
        webServer.content.add("{\"name\":\"Christmas Tree\",\"msg\":\"setSysinfo: [led:off]\",\"id\":34, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        Helog.run("log", webServer.getHostAndPort());
        assertThat(out.getContent()).isEqualTo(
                lines("2022-11-05 16:25:52.729  dev  info  34:Christmas Tree  setSysinfo: [led:off]"));
    }

    @Test
    public void toleratesSplitJson() {
        webServer.content.add("{\"name\":\"Christmas Tree\",\"msg\":\"setSysinfo: [led:off]\",\"id\":34, ");
        webServer.content.add("\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        Helog.run("log", webServer.getHostAndPort());
        assertThat(out.getContent()).isEqualTo(
                lines("2022-11-05 16:25:52.729  dev  info  34:Christmas Tree  setSysinfo: [led:off]"));
    }

    @Test
    public void rawSpoolsExact() {
        webServer.content.add("abc");
        webServer.content.add("def");
        Helog.run("log", webServer.getHostAndPort(), "--raw");
        assertThat(out.getContent()).isEqualTo("abcdef");
    }

    @Test
    public void exitCodeMinus1() {
        assertThat(Helog.run("log", webServer.getHostAndPort())).isEqualTo(-1);
    }
}
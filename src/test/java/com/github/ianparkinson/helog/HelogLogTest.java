package com.github.ianparkinson.helog;

import com.github.ianparkinson.helog.testing.StdErrExtension;
import com.github.ianparkinson.helog.testing.StdOutExtension;
import com.github.ianparkinson.helog.testing.WebSocketServerExtension;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.github.ianparkinson.helog.Helog.ERROR_PREFIX;
import static com.github.ianparkinson.helog.testing.TestStrings.ISO_OFFSET_DATE_TIME_MILLIS_REGEX;
import static com.github.ianparkinson.helog.testing.TestStrings.splitLines;
import static com.google.common.truth.Truth.assertThat;

public final class HelogLogTest {
    @RegisterExtension
    private final WebSocketServerExtension webServer = new WebSocketServerExtension();
    @RegisterExtension
    private final StdOutExtension out = new StdOutExtension();
    @RegisterExtension
    private final StdErrExtension err = new StdErrExtension();

    @Test
    public void connectsToLogSocket() throws InterruptedException {
        Helog.run("log", webServer.getHostAndPort());
        RecordedRequest request = webServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/logsocket");
    }

    @Test
    public void reportsConnection() {
        Helog.run("log", webServer.getHostAndPort());
        assertThat(splitLines(err.getContent())[0]).isEqualTo(
                "Connected to ws://" + webServer.getHostAndPort() + "/logsocket");
    }

    @Test
    public void writesFormattedJson() {
        webServer.content.add("{\"name\":\"Christmas Tree\",\"msg\":\"setSysinfo: led:off\",\"id\":34, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        Helog.run("log", webServer.getHostAndPort());

        String[] lines = splitLines(out.getContent());
        assertThat(lines).hasLength(1);
        assertThat(lines[0]).matches(
                ISO_OFFSET_DATE_TIME_MILLIS_REGEX + " info   dev 34 Christmas Tree  setSysinfo: led:off");
    }

    @Test
    public void toleratesSplitJson() {
        webServer.content.add("{\"name\":\"Christmas Tree\",\"msg\":\"setSysinfo: led:off\",\"id\":34, ");
        webServer.content.add("\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        Helog.run("log", webServer.getHostAndPort());

        String[] lines = splitLines(out.getContent());
        assertThat(lines).hasLength(1);
        assertThat(lines[0]).matches(
                ISO_OFFSET_DATE_TIME_MILLIS_REGEX + " info   dev 34 Christmas Tree  setSysinfo: led:off");
    }

    @Test
    public void filterIncludeDevice() {
        webServer.content.add("{\"name\":\"MatchByName\",\"msg\":\"msg\",\"id\":34, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"OtherDevice\",\"msg\":\"msg\",\"id\":35, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"MatchById\",\"msg\":\"msg\",\"id\":36, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"OtherApp\",\"msg\":\"msg\",\"id\":36, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"info\"}");

        Helog.run("log", webServer.getHostAndPort(), "--device=MatchByName,36");

        String[] lines = splitLines(out.getContent());
        assertThat(lines).hasLength(2);
        assertThat(lines[0]).contains("MatchByName");
        assertThat(lines[1]).contains("MatchById");
    }

    @Test
    public void filterExcludeDevice() {
        webServer.content.add("{\"name\":\"MatchByName\",\"msg\":\"msg\",\"id\":34, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"OtherDevice\",\"msg\":\"msg\",\"id\":35, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"MatchById\",\"msg\":\"msg\",\"id\":36, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"OtherApp\",\"msg\":\"msg\",\"id\":36, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"info\"}");

        Helog.run("log", webServer.getHostAndPort(), "--xdevice=MatchByName,36");

        String[] lines = splitLines(out.getContent());
        assertThat(lines).hasLength(2);
        assertThat(lines[0]).contains("OtherDevice");
        assertThat(lines[1]).contains("OtherApp");
    }

    @Test
    public void filterIncludeApp() {
        webServer.content.add("{\"name\":\"MatchByName\",\"msg\":\"msg\",\"id\":34, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"OtherApp\",\"msg\":\"msg\",\"id\":35, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"MatchById\",\"msg\":\"msg\",\"id\":36, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"OtherDevice\",\"msg\":\"msg\",\"id\":36, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        Helog.run("log", webServer.getHostAndPort(), "--app=MatchByName,36");
        String[] lines = splitLines(out.getContent());
        assertThat(lines).hasLength(2);
        assertThat(lines[0]).contains("MatchByName");
        assertThat(lines[1]).contains("MatchById");
    }

    @Test
    public void filterExcludeApp() {
        webServer.content.add("{\"name\":\"MatchByName\",\"msg\":\"msg\",\"id\":34, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"OtherApp\",\"msg\":\"msg\",\"id\":35, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"MatchById\",\"msg\":\"msg\",\"id\":36, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"OtherDevice\",\"msg\":\"msg\",\"id\":36, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        Helog.run("log", webServer.getHostAndPort(), "--xapp=MatchByName,36");
        String[] lines = splitLines(out.getContent());
        assertThat(lines).hasLength(2);
        assertThat(lines[0]).contains("OtherApp");
        assertThat(lines[1]).contains("OtherDevice");
    }

    @Test
    public void filterIncludeLogLevel() {
        webServer.content.add("{\"name\":\"FirstMatch\",\"msg\":\"msg\",\"id\":34, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"NoMatch\",\"msg\":\"msg\",\"id\":35, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"error\"}");
        webServer.content.add("{\"name\":\"SecondMatch\",\"msg\":\"msg\",\"id\":36, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"debug\"}");
        Helog.run("log", webServer.getHostAndPort(), "--level=info,debug");
        String[] lines = splitLines(out.getContent());
        assertThat(lines).hasLength(2);
        assertThat(lines[0]).contains("FirstMatch");
        assertThat(lines[1]).contains("SecondMatch");
    }

    @Test
    public void filterExcludeLogLevel() {
        webServer.content.add("{\"name\":\"FirstMatch\",\"msg\":\"msg\",\"id\":34, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"NoMatch\",\"msg\":\"msg\",\"id\":35, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"error\"}");
        webServer.content.add("{\"name\":\"SecondMatch\",\"msg\":\"msg\",\"id\":36, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"debug\"}");
        Helog.run("log", webServer.getHostAndPort(), "--xlevel=info,debug");
        String[] lines = splitLines(out.getContent());
        assertThat(lines).hasLength(1);
        assertThat(lines[0]).contains("NoMatch");
    }

    @Test
    public void writesInCsvFormat() {
        webServer.content.add("{\"name\":\"Christmas Tree\",\"msg\":\"setSysinfo: led:off\",\"id\":34, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        Helog.run("log", webServer.getHostAndPort(), "--csv");
        String[] lines = splitLines(out.getContent());
        assertThat(lines).hasLength(2);
        assertThat(lines[0]).isEqualTo("localTime,name,msg,id,time,type,level");
        assertThat(lines[1]).matches(ISO_OFFSET_DATE_TIME_MILLIS_REGEX +
                ",Christmas Tree,setSysinfo: led:off,34,2022-11-05 16:25:52.729,dev,info");
    }

    @Test
    public void rawSpoolsExact() {
        webServer.content.add("abc");
        webServer.content.add("def");
        Helog.run("log", webServer.getHostAndPort(), "--raw");
        assertThat(out.getContent()).isEqualTo("abcdef");
    }

    @Test
    public void rawAndCsvMutuallyExclusive() {
        int code = Helog.run("log", webServer.getHostAndPort(), "--raw", "--csv");
        assertThat(err.getContent()).startsWith(ERROR_PREFIX);
        assertThat(code).isEqualTo(2);
    }

    @Test
    public void filterValidationFailureHandled() {
        int code = Helog.run("log", webServer.getHostAndPort(), "--raw", "--device=42");
        assertThat(err.getContent()).startsWith(ERROR_PREFIX);
        assertThat(code).isEqualTo(2);
    }

    @Test
    public void exitCode1() {
        assertThat(Helog.run("log", webServer.getHostAndPort())).isEqualTo(1);
    }
}

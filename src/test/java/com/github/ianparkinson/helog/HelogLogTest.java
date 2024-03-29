package com.github.ianparkinson.helog;

import com.github.ianparkinson.helog.testing.StdErrExtension;
import com.github.ianparkinson.helog.testing.StdOutExtension;
import com.github.ianparkinson.helog.testing.WebSocketServerExtension;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;

import static com.github.ianparkinson.helog.Helog.ERROR_PREFIX;
import static com.github.ianparkinson.helog.testing.TestStrings.dropDateTime;
import static com.github.ianparkinson.helog.testing.TestStrings.splitLines;
import static com.google.common.truth.Truth.assertThat;

final class HelogLogTest {
    @RegisterExtension
    final WebSocketServerExtension webServer = new WebSocketServerExtension();
    @RegisterExtension
    final StdOutExtension out = new StdOutExtension();
    @RegisterExtension
    final StdErrExtension err = new StdErrExtension();

    @Test
    void connectsToLogSocket() throws InterruptedException {
        Helog.run("log", webServer.getHostAndPort());
        RecordedRequest request = webServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/logsocket");
    }

    @Test
    void reportsConnection() {
        Helog.run("log", webServer.getHostAndPort());
        assertThat(splitLines(err.getContent()).get(0)).isEqualTo(
                "Connected to ws://" + webServer.getHostAndPort() + "/logsocket");
    }

    @Test
    void writesFormattedJson() {
        webServer.content.add("{\"name\":\"Christmas Tree\",\"msg\":\"setSysinfo: led:off\",\"id\":34, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        Helog.run("log", webServer.getHostAndPort());

        List<String> lines = splitLines(out.getContent());
        assertThat(lines).hasSize(1);
        assertThat(dropDateTime(lines.get(0))).isEqualTo(" info   dev 34 Christmas Tree  setSysinfo: led:off");
    }

    @Test
    void filterIncludeDevice() {
        webServer.content.add("{\"name\":\"MatchByName\",\"msg\":\"msg\",\"id\":34, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"OtherDevice\",\"msg\":\"msg\",\"id\":35, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"MatchById\",\"msg\":\"msg\",\"id\":36, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"OtherApp\",\"msg\":\"msg\",\"id\":36, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"info\"}");

        Helog.run("log", webServer.getHostAndPort(), "--device=MatchByName,36");

        List<String> lines = splitLines(out.getContent());
        assertThat(lines).hasSize(2);
        assertThat(lines.get(0)).contains("MatchByName");
        assertThat(lines.get(1)).contains("MatchById");
    }

    @Test
    void filterExcludeDevice() {
        webServer.content.add("{\"name\":\"MatchByName\",\"msg\":\"msg\",\"id\":34, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"OtherDevice\",\"msg\":\"msg\",\"id\":35, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"MatchById\",\"msg\":\"msg\",\"id\":36, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"OtherApp\",\"msg\":\"msg\",\"id\":36, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"info\"}");

        Helog.run("log", webServer.getHostAndPort(), "--xdevice=MatchByName,36");

        List<String> lines = splitLines(out.getContent());
        assertThat(lines).hasSize(2);
        assertThat(lines.get(0)).contains("OtherDevice");
        assertThat(lines.get(1)).contains("OtherApp");
    }

    @Test
    void filterIncludeApp() {
        webServer.content.add("{\"name\":\"MatchByName\",\"msg\":\"msg\",\"id\":34, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"OtherApp\",\"msg\":\"msg\",\"id\":35, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"MatchById\",\"msg\":\"msg\",\"id\":36, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"OtherDevice\",\"msg\":\"msg\",\"id\":36, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        Helog.run("log", webServer.getHostAndPort(), "--app=MatchByName,36");
        List<String> lines = splitLines(out.getContent());
        assertThat(lines).hasSize(2);
        assertThat(lines.get(0)).contains("MatchByName");
        assertThat(lines.get(1)).contains("MatchById");
    }

    @Test
    void filterExcludeApp() {
        webServer.content.add("{\"name\":\"MatchByName\",\"msg\":\"msg\",\"id\":34, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"OtherApp\",\"msg\":\"msg\",\"id\":35, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"MatchById\",\"msg\":\"msg\",\"id\":36, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"OtherDevice\",\"msg\":\"msg\",\"id\":36, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        Helog.run("log", webServer.getHostAndPort(), "--xapp=MatchByName,36");
        List<String> lines = splitLines(out.getContent());
        assertThat(lines).hasSize(2);
        assertThat(lines.get(0)).contains("OtherApp");
        assertThat(lines.get(1)).contains("OtherDevice");
    }

    @Test
    void filterIncludeLogLevel() {
        webServer.content.add("{\"name\":\"FirstMatch\",\"msg\":\"msg\",\"id\":34, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"NoMatch\",\"msg\":\"msg\",\"id\":35, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"error\"}");
        webServer.content.add("{\"name\":\"SecondMatch\",\"msg\":\"msg\",\"id\":36, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"debug\"}");
        Helog.run("log", webServer.getHostAndPort(), "--level=info,debug");
        List<String> lines = splitLines(out.getContent());
        assertThat(lines).hasSize(2);
        assertThat(lines.get(0)).contains("FirstMatch");
        assertThat(lines.get(1)).contains("SecondMatch");
    }

    @Test
    void filterExcludeLogLevel() {
        webServer.content.add("{\"name\":\"FirstMatch\",\"msg\":\"msg\",\"id\":34, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"NoMatch\",\"msg\":\"msg\",\"id\":35, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"error\"}");
        webServer.content.add("{\"name\":\"SecondMatch\",\"msg\":\"msg\",\"id\":36, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"debug\"}");
        Helog.run("log", webServer.getHostAndPort(), "--xlevel=info,debug");
        List<String> lines = splitLines(out.getContent());
        assertThat(lines).hasSize(1);
        assertThat(lines.get(0)).contains("NoMatch");
    }

    @Test
    void writesInCsvFormat() {
        webServer.content.add("{\"name\":\"Christmas Tree\",\"msg\":\"setSysinfo: led:off\",\"id\":34, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        Helog.run("log", webServer.getHostAndPort(), "--csv");
        List<String> lines = splitLines(out.getContent());
        assertThat(lines).hasSize(2);
        assertThat(lines.get(0)).isEqualTo("localTime,name,msg,id,time,type,level");
        assertThat(dropDateTime(lines.get(1))).isEqualTo(
                ",Christmas Tree,setSysinfo: led:off,34,2022-11-05 16:25:52.729,dev,info");
    }

    @Test
    void toleratesMalformedJson() {
        webServer.content.add("unparseable");
        webServer.content.add("{\"name\":\"Christmas Tree\",\"msg\":\"setSysinfo: led:off\",\"id\":34, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        Helog.run("log", webServer.getHostAndPort());

        List<String> errLines = splitLines(err.getContent());
        assertThat(errLines.get(1)).contains("Malformed JSON");
        assertThat(errLines.get(2)).isEqualTo("unparseable");

        List<String> lines = splitLines(out.getContent());
        assertThat(lines).hasSize(1);
        assertThat(dropDateTime(lines.get(0))).isEqualTo(" info   dev 34 Christmas Tree  setSysinfo: led:off");
    }

    @Test
    void rawSpoolsExact() {
        webServer.content.add("abcdef");
        Helog.run("log", webServer.getHostAndPort(), "--raw");

        List<String> lines = splitLines(out.getContent());
        assertThat(lines).containsExactly("abcdef");
    }

    @Test
    void rawAndCsvMutuallyExclusive() {
        int code = Helog.run("log", webServer.getHostAndPort(), "--raw", "--csv");
        assertThat(err.getContent()).startsWith(ERROR_PREFIX);
        assertThat(code).isEqualTo(2);
    }

    @Test
    void filterValidationFailureHandled() {
        int code = Helog.run("log", webServer.getHostAndPort(), "--raw", "--device=42");
        assertThat(err.getContent()).startsWith(ERROR_PREFIX);
        assertThat(code).isEqualTo(2);
    }

    @Test
    void exitCode1() {
        assertThat(Helog.run("log", webServer.getHostAndPort())).isEqualTo(1);
    }
}

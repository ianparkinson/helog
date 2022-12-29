package com.github.ianparkinson.helog;

import com.github.ianparkinson.helog.testing.StdErrExtension;
import com.github.ianparkinson.helog.testing.StdOutExtension;
import com.github.ianparkinson.helog.testing.WebSocketServerExtension;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.github.ianparkinson.helog.Helog.ERROR_PREFIX;
import static com.github.ianparkinson.helog.testing.TestStrings.lines;
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
        webServer.content.add("{\"name\":\"Christmas Tree\",\"msg\":\"setSysinfo: [led:off]\",\"id\":34, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        Helog.run("log", webServer.getHostAndPort());
        assertThat(out.getContent()).isEqualTo(
                lines("2022-11-05 16:25:52.729 info   dev 34 Christmas Tree  setSysinfo: [led:off]"));
    }

    @Test
    public void toleratesSplitJson() {
        webServer.content.add("{\"name\":\"Christmas Tree\",\"msg\":\"setSysinfo: [led:off]\",\"id\":34, ");
        webServer.content.add("\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        Helog.run("log", webServer.getHostAndPort());
        assertThat(out.getContent()).isEqualTo(
                lines("2022-11-05 16:25:52.729 info   dev 34 Christmas Tree  setSysinfo: [led:off]"));
    }

    @Test
    public void deviceMatchesName() {
        webServer.content.add("{\"name\":\"Christmas Tree\",\"msg\":\"setSysinfo: [led:off]\",\"id\":34, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        Helog.run("log", webServer.getHostAndPort(), "--device=\"Christmas Tree\"");
        assertThat(out.getContent()).isNotEmpty();
    }

    @Test
    public void deviceMatchesId() {
        webServer.content.add("{\"name\":\"Christmas Tree\",\"msg\":\"setSysinfo: [led:off]\",\"id\":34, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        Helog.run("log", webServer.getHostAndPort(), "--device=34");
        assertThat(out.getContent()).isNotEmpty();
    }

    @Test
    public void deviceMatchesNeitherNameNorId() {
        webServer.content.add("{\"name\":\"Christmas Tree\",\"msg\":\"setSysinfo: [led:off]\",\"id\":34, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        Helog.run("log", webServer.getHostAndPort(), "--device=23");
        assertThat(out.getContent()).isEmpty();
    }

    @Test
    public void excludeDeviceByName() {
        webServer.content.add("{\"name\":\"NoMatch\",\"msg\":\"DeviceNoMatch]\",\"id\":34, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"Match\",\"msg\":\"DeviceMatch\",\"id\":35, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"Match\",\"msg\":\"AppMatch\",\"id\":35, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"info\"}");
        Helog.run("log", webServer.getHostAndPort(), "--xdevice=Match");
        String[] lines = splitLines(out.getContent());
        assertThat(lines).hasLength(2);
        assertThat(lines[0]).contains("DeviceNoMatch");
        assertThat(lines[1]).contains("AppMatch");
    }

    @Test
    public void excludeDeviceById() {
        webServer.content.add("{\"name\":\"NoMatch\",\"msg\":\"DeviceNoMatch]\",\"id\":34, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"Match\",\"msg\":\"DeviceMatch\",\"id\":35, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"Match\",\"msg\":\"AppMatch\",\"id\":35, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"info\"}");
        Helog.run("log", webServer.getHostAndPort(), "--xdevice=35");
        String[] lines = splitLines(out.getContent());
        assertThat(lines).hasLength(2);
        assertThat(lines[0]).contains("DeviceNoMatch");
        assertThat(lines[1]).contains("AppMatch");
    }

    @Test
    public void excludeDeviceMultipleIds() {
        webServer.content.add("{\"name\":\"ThirtyFour\",\"msg\":\"ThirtyFour]\",\"id\":34, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"ThirtyFive\",\"msg\":\"ThirtyFive\",\"id\":35, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"ThirtySix\",\"msg\":\"ThirtySix\",\"id\":36, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        Helog.run("log", webServer.getHostAndPort(), "--xdevice=34,36");
        String[] lines = splitLines(out.getContent());
        assertThat(lines).hasLength(1);
        assertThat(lines[0]).contains("ThirtyFive");
    }

    @Test
    public void appMatchesName() {
        webServer.content.add("{\"name\":\"Christmas Tree\",\"msg\":\"setSysinfo: [led:off]\",\"id\":34, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"info\"}");
        Helog.run("log", webServer.getHostAndPort(), "--app=\"Christmas Tree\"");
        assertThat(out.getContent()).isNotEmpty();
    }

    @Test
    public void appMatchesId() {
        webServer.content.add("{\"name\":\"Christmas Tree\",\"msg\":\"setSysinfo: [led:off]\",\"id\":34, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"info\"}");
        Helog.run("log", webServer.getHostAndPort(), "--app=34");
        assertThat(out.getContent()).isNotEmpty();
    }

    @Test
    public void appMatchesNeitherNameNorId() {
        webServer.content.add("{\"name\":\"Christmas Tree\",\"msg\":\"setSysinfo: [led:off]\",\"id\":34, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"info\"}");
        Helog.run("log", webServer.getHostAndPort(), "--app=23");
        assertThat(out.getContent()).isEmpty();
    }

    @Test
    public void appMultipleIds() {
        webServer.content.add("{\"name\":\"ThirtyFour\",\"msg\":\"setSysinfo: [led:off]\",\"id\":34, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"ThirtyFive\",\"msg\":\"setSysinfo: [led:off]\",\"id\":35, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"ThirtySix\",\"msg\":\"setSysinfo: [led:off]\",\"id\":36, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"info\"}");
        Helog.run("log", webServer.getHostAndPort(), "--app=34,36");
        String[] lines = splitLines(out.getContent());
        assertThat(lines).hasLength(2);
        assertThat(lines[0]).contains("ThirtyFour");
        assertThat(lines[1]).contains("ThirtySix");
    }

    @Test
    public void excludeAppByName() {
        webServer.content.add("{\"name\":\"ThirtyFour\",\"msg\":\"AppNoMatch\",\"id\":34, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"ThirtyFive\",\"msg\":\"AppMatch\",\"id\":35, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"ThirtyFive\",\"msg\":\"DeviceMatch\",\"id\":35, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        Helog.run("log", webServer.getHostAndPort(), "--xapp=ThirtyFive");
        String[] lines = splitLines(out.getContent());
        assertThat(lines).hasLength(2);
        assertThat(lines[0]).contains("AppNoMatch");
        assertThat(lines[1]).contains("DeviceMatch");
    }

    @Test
    public void excludeAppById() {
        webServer.content.add("{\"name\":\"ThirtyFour\",\"msg\":\"AppNoMatch\",\"id\":34, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"ThirtyFive\",\"msg\":\"AppMatch\",\"id\":35, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"ThirtyFive\",\"msg\":\"DeviceMatch\",\"id\":35, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        Helog.run("log", webServer.getHostAndPort(), "--xapp=35");
        String[] lines = splitLines(out.getContent());
        assertThat(lines).hasLength(2);
        assertThat(lines[0]).contains("AppNoMatch");
        assertThat(lines[1]).contains("DeviceMatch");
    }

    @Test
    public void excludeAppMultipleIds() {
        webServer.content.add("{\"name\":\"ThirtyFour\",\"msg\":\"ThirtyFour\",\"id\":34, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"ThirtyFive\",\"msg\":\"ThirtyFive\",\"id\":35, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"ThirtySix\",\"msg\":\"ThirtySix\",\"id\":36, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"info\"}");
        Helog.run("log", webServer.getHostAndPort(), "--xapp=34,36");
        String[] lines = splitLines(out.getContent());
        assertThat(lines).hasLength(1);
        assertThat(lines[0]).contains("ThirtyFive");
    }

    @Test
    public void deviceAndApp() {
        webServer.content.add("{\"name\":\"ThirtyFour\",\"msg\":\"setSysinfo: [led:off]\",\"id\":34, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"ThirtyFive\",\"msg\":\"setSysinfo: [led:off]\",\"id\":35, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"app\",\"level\":\"info\"}");
        webServer.content.add("{\"name\":\"ThirtySix\",\"msg\":\"setSysinfo: [led:off]\",\"id\":36, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        Helog.run("log", webServer.getHostAndPort(), "--app=34", "--device=36");
        String[] lines = splitLines(out.getContent());
        assertThat(lines).hasLength(2);
        assertThat(lines[0]).contains("ThirtyFour");
        assertThat(lines[1]).contains("ThirtySix");
    }

    @Test
    public void inclusiveAndExclusiveDeviceDisallowed() {
        int code = Helog.run("log", webServer.getHostAndPort(), "--device=43", "--xdevice=42");
        assertThat(err.getContent()).startsWith(ERROR_PREFIX);
        assertThat(code).isEqualTo(2);
    }

    @Test
    public void inclusiveAndExclusiveAppDisallowed() {
        int code = Helog.run("log", webServer.getHostAndPort(), "--app=43", "--xapp=42");
        assertThat(err.getContent()).startsWith(ERROR_PREFIX);
        assertThat(code).isEqualTo(2);
    }

    @Test
    public void writesInCsvFormat() {
        webServer.content.add("{\"name\":\"Christmas Tree\",\"msg\":\"setSysinfo: [led:off]\",\"id\":34, " +
                "\"time\":\"2022-11-05 16:25:52.729\",\"type\":\"dev\",\"level\":\"info\"}");
        Helog.run("log", webServer.getHostAndPort(), "--csv");
        assertThat(out.getContent()).isEqualTo(lines(
                "name,msg,id,time,type,level",
                "Christmas Tree,setSysinfo: [led:off],34,2022-11-05 16:25:52.729,dev,info"));
    }

    @Test
    public void rawSpoolsExact() {
        webServer.content.add("abc");
        webServer.content.add("def");
        Helog.run("log", webServer.getHostAndPort(), "--raw");
        assertThat(out.getContent()).isEqualTo("abcdef");
    }

    @Test
    public void rawDisallowsDevice() {
        int code = Helog.run("log", webServer.getHostAndPort(), "--raw", "--device=42");
        assertThat(err.getContent()).startsWith(ERROR_PREFIX);
        assertThat(code).isEqualTo(2);
    }

    @Test
    public void rawDisallowsXDevice() {
        int code = Helog.run("log", webServer.getHostAndPort(), "--raw", "--xdevice=42");
        assertThat(err.getContent()).startsWith(ERROR_PREFIX);
        assertThat(code).isEqualTo(2);
    }

    @Test
    public void rawDisallowsApp() {
        int code = Helog.run("log", webServer.getHostAndPort(), "--raw", "--app=42");
        assertThat(err.getContent()).startsWith(ERROR_PREFIX);
        assertThat(code).isEqualTo(2);
    }

    @Test
    public void rawDisallowsXApp() {
        int code = Helog.run("log", webServer.getHostAndPort(), "--raw", "--xapp=42");
        assertThat(err.getContent()).startsWith(ERROR_PREFIX);
        assertThat(code).isEqualTo(2);
    }

    @Test
    public void rawAndCsvMutuallyExclusive() {
        int code = Helog.run("log", webServer.getHostAndPort(), "--raw", "--csv");
        assertThat(err.getContent()).startsWith(ERROR_PREFIX);
        assertThat(code).isEqualTo(2);
    }

    @Test
    public void exitCode1() {
        assertThat(Helog.run("log", webServer.getHostAndPort())).isEqualTo(1);
    }
}

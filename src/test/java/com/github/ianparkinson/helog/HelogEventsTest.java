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
    public void reportsConnection() {
        Helog.run("events", webServer.getHostAndPort());
        assertThat(splitLines(err.getContent())[0]).isEqualTo(
                "Connected to ws://" + webServer.getHostAndPort() + "/eventsocket");
    }

    @Test
    public void writesFormattedJson_device() {
        webServer.content.add("{ \"source\":\"DEVICE\",\"name\":\"switch\",\"displayName\" : \"Christmas Tree\", " +
                "\"value\" : \"off\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":34,\"hubId\":0," +
                "\"installedAppId\":0,\"descriptionText\" : \"null\"}");
        Helog.run("events", webServer.getHostAndPort());
        assertThat(out.getContent()).isEqualTo(lines("DEVICE 34 Christmas Tree: switch off"));
    }

    @Test
    public void writesFormattedJson_app() {
        webServer.content.add("{ \"source\":\"APP\",\"name\":\"eventWithDescription\",\"displayName\" : \"null\", " +
                "\"value\" : \"5\", \"type\" : \"null\", \"unit\":\"jiffy\",\"deviceId\":0,\"hubId\":0," +
                "\"installedAppId\":93,\"descriptionText\" : \"This is an event\"}");
        Helog.run("events", webServer.getHostAndPort());
        assertThat(out.getContent()).isEqualTo(lines("APP    93: eventWithDescription 5 jiffy This is an event"));
    }

    @Test
    public void toleratesSplitJson() {
        webServer.content.add("{ \"source\":\"DEVICE\",\"name\":\"switch\",\"displayName\" : \"Christmas Tree\", ");
        webServer.content.add("\"value\" : \"off\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":34,");
        webServer.content.add("\"hubId\":0,\"installedAppId\":0,\"descriptionText\" : \"null\"}");
        Helog.run("events", webServer.getHostAndPort());
        assertThat(out.getContent()).isEqualTo(lines("DEVICE 34 Christmas Tree: switch off"));
    }

    @Test
    public void deviceMatchesName() {
        webServer.content.add("{ \"source\":\"DEVICE\",\"name\":\"switch\",\"displayName\" : \"Christmas Tree\", " +
                "\"value\" : \"off\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":34,\"hubId\":0," +
                "\"installedAppId\":0,\"descriptionText\" : \"null\"}");
        Helog.run("events", webServer.getHostAndPort(), "--device=\"Christmas Tree\"");
        assertThat(out.getContent()).isNotEmpty();
    }

    @Test
    public void deviceMatchesId() {
        webServer.content.add("{ \"source\":\"DEVICE\",\"name\":\"switch\",\"displayName\" : \"Christmas Tree\", " +
                "\"value\" : \"off\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":34,\"hubId\":0," +
                "\"installedAppId\":0,\"descriptionText\" : \"null\"}");
        Helog.run("events", webServer.getHostAndPort(), "--device=34");
        assertThat(out.getContent()).isNotEmpty();
    }

    @Test
    public void deviceMatchesNeitherNameNorId() {
        webServer.content.add("{ \"source\":\"DEVICE\",\"name\":\"switch\",\"displayName\" : \"Christmas Tree\", " +
                "\"value\" : \"off\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":34,\"hubId\":0," +
                "\"installedAppId\":0,\"descriptionText\" : \"null\"}");
        Helog.run("events", webServer.getHostAndPort(), "--device=23");
        assertThat(out.getContent()).isEmpty();
    }

    @Test
    public void deviceMultipleIds() {
        webServer.content.add("{ \"source\":\"DEVICE\",\"name\":\"switch\",\"displayName\" : \"ThirtyFour\", " +
                "\"value\" : \"off\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":34,\"hubId\":0," +
                "\"installedAppId\":0,\"descriptionText\" : \"null\"}");
        webServer.content.add("{ \"source\":\"DEVICE\",\"name\":\"switch\",\"displayName\" : \"ThirtyFive\", " +
                "\"value\" : \"off\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":35,\"hubId\":0," +
                "\"installedAppId\":0,\"descriptionText\" : \"null\"}");
        webServer.content.add("{ \"source\":\"DEVICE\",\"name\":\"switch\",\"displayName\" : \"ThirtySix\", " +
                "\"value\" : \"off\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":36,\"hubId\":0," +
                "\"installedAppId\":0,\"descriptionText\" : \"null\"}");
        Helog.run("events", webServer.getHostAndPort(), "--device=34,36");
        String[] lines = splitLines(out.getContent());
        assertThat(lines).hasLength(2);
        assertThat(lines[0]).contains("ThirtyFour");
        assertThat(lines[1]).contains("ThirtySix");
    }

    @Test
    public void appMatchesId() {
        webServer.content.add("{ \"source\":\"APP\",\"name\":\"switch\",\"displayName\" : \"\", " +
                "\"value\" : \"off\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":0,\"hubId\":0," +
                "\"installedAppId\":34,\"descriptionText\" : \"null\"}");
        Helog.run("events", webServer.getHostAndPort(), "--app=34");
        assertThat(out.getContent()).isNotEmpty();
    }

    @Test
    public void appNoMatch() {
        webServer.content.add("{ \"source\":\"APP\",\"name\":\"switch\",\"displayName\" : \"\", " +
                "\"value\" : \"off\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":0,\"hubId\":0," +
                "\"installedAppId\":34,\"descriptionText\" : \"null\"}");
        Helog.run("events", webServer.getHostAndPort(), "--app=23");
        assertThat(out.getContent()).isEmpty();
    }

    @Test
    public void appMultipleIds() {
        webServer.content.add("{ \"source\":\"APP\",\"name\":\"switch\",\"displayName\" : \"\", " +
                "\"value\" : \"ThirtyFour\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":0,\"hubId\":0," +
                "\"installedAppId\":34,\"descriptionText\" : \"null\"}");
        webServer.content.add("{ \"source\":\"APP\",\"name\":\"switch\",\"displayName\" : \"\", " +
                "\"value\" : \"ThirtyFive\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":0,\"hubId\":0," +
                "\"installedAppId\":35,\"descriptionText\" : \"null\"}");
        webServer.content.add("{ \"source\":\"APP\",\"name\":\"switch\",\"displayName\" : \"\", " +
                "\"value\" : \"ThirtySix\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":0,\"hubId\":0," +
                "\"installedAppId\":36,\"descriptionText\" : \"null\"}");
        Helog.run("events", webServer.getHostAndPort(), "--app=34,36");
        String[] lines = splitLines(out.getContent());
        assertThat(lines).hasLength(2);
        assertThat(lines[0]).contains("ThirtyFour");
        assertThat(lines[1]).contains("ThirtySix");
    }

    @Test
    public void appDisallowsName() {
        int code = Helog.run("events", webServer.getHostAndPort(), "--app=SomeApp");
        assertThat(err.getContent()).startsWith(ERROR_PREFIX);
        assertThat(code).isEqualTo(2);
    }

    @Test
    public void deviceAndApp() {
        webServer.content.add("{ \"source\":\"APP\",\"name\":\"switch\",\"displayName\" : \"\", " +
                "\"value\" : \"ThirtyFour\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":0,\"hubId\":0," +
                "\"installedAppId\":34,\"descriptionText\" : \"null\"}");
        webServer.content.add("{ \"source\":\"APP\",\"name\":\"switch\",\"displayName\" : \"\", " +
                "\"value\" : \"ThirtyFive\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":0,\"hubId\":0," +
                "\"installedAppId\":35,\"descriptionText\" : \"null\"}");
        webServer.content.add("{ \"source\":\"DEVICE\",\"name\":\"switch\",\"displayName\" : \"Christmas Tree\", " +
                "\"value\" : \"ThirtySix\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":36,\"hubId\":0," +
                "\"installedAppId\":0,\"descriptionText\" : \"null\"}");
        Helog.run("events", webServer.getHostAndPort(), "--app=34", "--device=36");
        String[] lines = splitLines(out.getContent());
        assertThat(lines).hasLength(2);
        assertThat(lines[0]).contains("ThirtyFour");
        assertThat(lines[1]).contains("ThirtySix");
    }

    @Test
    public void writesInCsvFormat() {
        webServer.content.add("{ \"source\":\"DEVICE\",\"name\":\"switch\",\"displayName\" : \"Christmas Tree\", " +
                "\"value\" : \"off\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":34,\"hubId\":0," +
                "\"installedAppId\":0,\"descriptionText\" : \"null\"}");
        Helog.run("events", webServer.getHostAndPort(), "--csv");
        assertThat(out.getContent()).isEqualTo(lines(
                "source,name,displayName,value,type,unit,deviceId,hubId,installedAppId,descriptionText",
                "DEVICE,switch,Christmas Tree,off,digital,,34,0,0,"));
    }

    @Test
    public void rawSpoolsExact() {
        webServer.content.add("abc");
        webServer.content.add("def");
        Helog.run("events", webServer.getHostAndPort(), "--raw");
        assertThat(out.getContent()).isEqualTo("abcdef");
    }

    @Test
    public void rawDisallowsDevice() {
        int code = Helog.run("events", webServer.getHostAndPort(), "--raw", "--device=42");
        assertThat(err.getContent()).startsWith(ERROR_PREFIX);
        assertThat(code).isEqualTo(2);
    }

    @Test
    public void rawDisallowsApp() {
        int code = Helog.run("events", webServer.getHostAndPort(), "--raw", "--app=42");
        assertThat(err.getContent()).startsWith(ERROR_PREFIX);
        assertThat(code).isEqualTo(2);
    }

    @Test
    public void rawAndCsvMutuallyExclusive() {
        int code = Helog.run("events", webServer.getHostAndPort(), "--raw", "--csv");
        assertThat(err.getContent()).startsWith(ERROR_PREFIX);
        assertThat(code).isEqualTo(2);
    }

    @Test
    public void exitCode1() {
        assertThat(Helog.run("events", webServer.getHostAndPort())).isEqualTo(1);
    }
}

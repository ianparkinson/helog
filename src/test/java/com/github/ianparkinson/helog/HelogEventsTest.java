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

final class HelogEventsTest {
    @RegisterExtension
    final WebSocketServerExtension webServer = new WebSocketServerExtension();
    @RegisterExtension
    final StdOutExtension out = new StdOutExtension();
    @RegisterExtension
    final StdErrExtension err = new StdErrExtension();

    @Test
    void connectsToEventSocket() throws InterruptedException {
        Helog.run("events", webServer.getHostAndPort());
        RecordedRequest request = webServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/eventsocket");
    }

    @Test
    void reportsConnection() {
        Helog.run("events", webServer.getHostAndPort());
        assertThat(splitLines(err.getContent()).get(0)).isEqualTo(
                "Connected to ws://" + webServer.getHostAndPort() + "/eventsocket");
    }

    @Test
    void writesFormattedJson_device() {
        webServer.content.add("{ \"source\":\"DEVICE\",\"name\":\"switch\",\"displayName\" : \"Christmas Tree\", " +
                "\"value\" : \"off\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":34,\"hubId\":0," +
                "\"installedAppId\":0,\"descriptionText\" : \"null\"}");
        Helog.run("events", webServer.getHostAndPort());

        List<String> lines = splitLines(out.getContent());
        assertThat(lines).hasSize(1);
        assertThat(dropDateTime(lines.get(0))).isEqualTo(" DEVICE 34 Christmas Tree: switch off");
    }

    @Test
    void writesFormattedJson_app() {
        webServer.content.add("{ \"source\":\"APP\",\"name\":\"eventWithDescription\",\"displayName\" : \"null\", " +
                "\"value\" : \"5\", \"type\" : \"null\", \"unit\":\"jiffy\",\"deviceId\":0,\"hubId\":0," +
                "\"installedAppId\":93,\"descriptionText\" : \"This is an event\"}");
        Helog.run("events", webServer.getHostAndPort());

        List<String> lines = splitLines(out.getContent());
        assertThat(lines).hasSize(1);
        assertThat(dropDateTime(lines.get(0))).isEqualTo(" APP    93: eventWithDescription 5 jiffy This is an event");
    }

    @Test
    void filterIncludeDevice() {
        webServer.content.add("{ \"source\":\"DEVICE\",\"name\":\"switch\",\"displayName\" : \"MatchByName\", " +
                "\"value\" : \"off\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":34,\"hubId\":0," +
                "\"installedAppId\":0,\"descriptionText\" : \"null\"}");
        webServer.content.add("{ \"source\":\"DEVICE\",\"name\":\"switch\",\"displayName\" : \"OtherDevice\", " +
                "\"value\" : \"off\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":35,\"hubId\":0," +
                "\"installedAppId\":0,\"descriptionText\" : \"null\"}");
        webServer.content.add("{ \"source\":\"DEVICE\",\"name\":\"switch\",\"displayName\" : \"MatchById\", " +
                "\"value\" : \"off\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":36,\"hubId\":0," +
                "\"installedAppId\":0,\"descriptionText\" : \"null\"}");
        webServer.content.add("{ \"source\":\"APP\",\"name\":\"OtherApp\",\"displayName\" : \"\", " +
                "\"value\" : \"off\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":0,\"hubId\":0," +
                "\"installedAppId\":35,\"descriptionText\" : \"null\"}");

        Helog.run("events", webServer.getHostAndPort(), "--device=MatchByName,36");

        List<String> lines = splitLines(out.getContent());
        assertThat(lines).hasSize(2);
        assertThat(lines.get(0)).contains("MatchByName");
        assertThat(lines.get(1)).contains("MatchById");
    }

    @Test
    void filterExcludeDevice() {
        webServer.content.add("{ \"source\":\"DEVICE\",\"name\":\"switch\",\"displayName\" : \"MatchByName\", " +
                "\"value\" : \"off\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":34,\"hubId\":0," +
                "\"installedAppId\":0,\"descriptionText\" : \"null\"}");
        webServer.content.add("{ \"source\":\"DEVICE\",\"name\":\"switch\",\"displayName\" : \"OtherDevice\", " +
                "\"value\" : \"off\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":35,\"hubId\":0," +
                "\"installedAppId\":0,\"descriptionText\" : \"null\"}");
        webServer.content.add("{ \"source\":\"DEVICE\",\"name\":\"switch\",\"displayName\" : \"MatchById\", " +
                "\"value\" : \"off\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":36,\"hubId\":0," +
                "\"installedAppId\":0,\"descriptionText\" : \"null\"}");
        webServer.content.add("{ \"source\":\"APP\",\"name\":\"OtherApp\",\"displayName\" : \"\", " +
                "\"value\" : \"off\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":0,\"hubId\":0," +
                "\"installedAppId\":36,\"descriptionText\" : \"null\"}");

        Helog.run("events", webServer.getHostAndPort(), "--xdevice=MatchByName,36");

        List<String> lines = splitLines(out.getContent());
        assertThat(lines).hasSize(2);
        assertThat(lines.get(0)).contains("OtherDevice");
        assertThat(lines.get(1)).contains("OtherApp");
    }

    @Test
    void filterIncludeApp() {
        webServer.content.add("{ \"source\":\"APP\",\"name\":\"FirstMatch\",\"displayName\" : \"\", " +
                "\"value\" : \"off\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":0,\"hubId\":0," +
                "\"installedAppId\":34,\"descriptionText\" : \"null\"}");
        webServer.content.add("{ \"source\":\"APP\",\"name\":\"OtherApp\",\"displayName\" : \"\", " +
                "\"value\" : \"off\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":0,\"hubId\":0," +
                "\"installedAppId\":35,\"descriptionText\" : \"null\"}");
        webServer.content.add("{ \"source\":\"APP\",\"name\":\"SecondMatch\",\"displayName\" : \"\", " +
                "\"value\" : \"off\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":0,\"hubId\":0," +
                "\"installedAppId\":36,\"descriptionText\" : \"null\"}");
        webServer.content.add("{ \"source\":\"DEVICE\",\"name\":\"switch\",\"displayName\" : \"OtherDevice\", " +
                "\"value\" : \"off\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":34,\"hubId\":0," +
                "\"installedAppId\":0,\"descriptionText\" : \"null\"}");

        Helog.run("events", webServer.getHostAndPort(), "--app=34,36");

        List<String> lines = splitLines(out.getContent());
        assertThat(lines).hasSize(2);
        assertThat(lines.get(0)).contains("FirstMatch");
        assertThat(lines.get(1)).contains("SecondMatch");
    }

    @Test
    void filterExcludeApp() {
        webServer.content.add("{ \"source\":\"APP\",\"name\":\"FirstMatch\",\"displayName\" : \"\", " +
                "\"value\" : \"off\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":0,\"hubId\":0," +
                "\"installedAppId\":34,\"descriptionText\" : \"null\"}");
        webServer.content.add("{ \"source\":\"APP\",\"name\":\"OtherApp\",\"displayName\" : \"\", " +
                "\"value\" : \"off\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":0,\"hubId\":0," +
                "\"installedAppId\":35,\"descriptionText\" : \"null\"}");
        webServer.content.add("{ \"source\":\"APP\",\"name\":\"SecondMatch\",\"displayName\" : \"\", " +
                "\"value\" : \"off\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":0,\"hubId\":0," +
                "\"installedAppId\":36,\"descriptionText\" : \"null\"}");
        webServer.content.add("{ \"source\":\"DEVICE\",\"name\":\"switch\",\"displayName\" : \"OtherDevice\", " +
                "\"value\" : \"off\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":35,\"hubId\":0," +
                "\"installedAppId\":0,\"descriptionText\" : \"null\"}");

        Helog.run("events", webServer.getHostAndPort(), "--xapp=34,36");

        List<String> lines = splitLines(out.getContent());
        assertThat(lines).hasSize(2);
        assertThat(lines.get(0)).contains("OtherApp");
        assertThat(lines.get(1)).contains("OtherDevice");
    }

    @Test
    void filterIncludeName() {
        webServer.content.add("{ \"source\":\"APP\",\"name\":\"Prop1\",\"displayName\" : \"\", " +
                "\"value\" : \"off\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":0,\"hubId\":0," +
                "\"installedAppId\":34,\"descriptionText\" : \"FirstMatch\"}");
        webServer.content.add("{ \"source\":\"APP\",\"name\":\"Prop2\",\"displayName\" : \"\", " +
                "\"value\" : \"off\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":0,\"hubId\":0," +
                "\"installedAppId\":35,\"descriptionText\" : \"NoMatch\"}");
        webServer.content.add("{ \"source\":\"APP\",\"name\":\"Prop3\",\"displayName\" : \"\", " +
                "\"value\" : \"off\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":0,\"hubId\":0," +
                "\"installedAppId\":36,\"descriptionText\" : \"SecondMatch\"}");

        Helog.run("events", webServer.getHostAndPort(), "--name=Prop1,Prop3");

        List<String> lines = splitLines(out.getContent());
        assertThat(lines).hasSize(2);
        assertThat(lines.get(0)).contains("FirstMatch");
        assertThat(lines.get(1)).contains("SecondMatch");
    }

    @Test
    void filterExcludeName() {
        webServer.content.add("{ \"source\":\"APP\",\"name\":\"Prop1\",\"displayName\" : \"\", " +
                "\"value\" : \"off\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":0,\"hubId\":0," +
                "\"installedAppId\":34,\"descriptionText\" : \"FirstMatch\"}");
        webServer.content.add("{ \"source\":\"APP\",\"name\":\"Prop2\",\"displayName\" : \"\", " +
                "\"value\" : \"off\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":0,\"hubId\":0," +
                "\"installedAppId\":35,\"descriptionText\" : \"NoMatch\"}");
        webServer.content.add("{ \"source\":\"APP\",\"name\":\"Prop3\",\"displayName\" : \"\", " +
                "\"value\" : \"off\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":0,\"hubId\":0," +
                "\"installedAppId\":36,\"descriptionText\" : \"SecondMatch\"}");

        Helog.run("events", webServer.getHostAndPort(), "--xname=Prop1,Prop3");

        List<String> lines = splitLines(out.getContent());
        assertThat(lines).hasSize(1);
        assertThat(lines.get(0)).contains("NoMatch");
    }

    @Test
    void writesInCsvFormat() {
        webServer.content.add("{ \"source\":\"DEVICE\",\"name\":\"switch\",\"displayName\" : \"Christmas Tree\", " +
                "\"value\" : \"off\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":34,\"hubId\":0," +
                "\"installedAppId\":0,\"descriptionText\" : \"null\"}");
        Helog.run("events", webServer.getHostAndPort(), "--csv");
        List<String> lines = splitLines(out.getContent());
        assertThat(lines).hasSize(2);
        assertThat(lines.get(0)).isEqualTo(
                "localTime,source,name,displayName,value,type,unit,deviceId,hubId,installedAppId,descriptionText");
        assertThat(dropDateTime(lines.get(1))).isEqualTo(",DEVICE,switch,Christmas Tree,off,digital,null,34,0,0,null");
    }

    @Test
    void toleratesMalformedJson() {
        webServer.content.add("unparseable");
        webServer.content.add("{ \"source\":\"DEVICE\",\"name\":\"switch\",\"displayName\" : \"Christmas Tree\", " +
                "\"value\" : \"off\", \"type\" : \"digital\", \"unit\":\"null\",\"deviceId\":34,\"hubId\":0," +
                "\"installedAppId\":0,\"descriptionText\" : \"null\"}");
        Helog.run("events", webServer.getHostAndPort());

        List<String> errLines = splitLines(err.getContent());
        assertThat(errLines.get(1)).contains("Malformed JSON");
        assertThat(errLines.get(2)).isEqualTo("unparseable");

        List<String> lines = splitLines(out.getContent());
        assertThat(lines).hasSize(1);
        assertThat(dropDateTime(lines.get(0))).isEqualTo(" DEVICE 34 Christmas Tree: switch off");
    }

    @Test
    void rawSpoolsExact() {
        webServer.content.add("abcdef");
        Helog.run("events", webServer.getHostAndPort(), "--raw");

        List<String> lines = splitLines(out.getContent());
        assertThat(lines).containsExactly("abcdef");
    }

    @Test
    void rawAndCsvMutuallyExclusive() {
        int code = Helog.run("events", webServer.getHostAndPort(), "--raw", "--csv");
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
        assertThat(Helog.run("events", webServer.getHostAndPort())).isEqualTo(1);
    }
}

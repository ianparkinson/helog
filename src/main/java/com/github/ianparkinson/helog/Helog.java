package com.github.ianparkinson.helog;

import picocli.CommandLine;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "helog",
        header = "Writes live logs from a Hubitat Elevation's /logsocket or /eventsocket streams to stdout.",
        synopsisHeading = "Usage:%n",
        customSynopsis = {
                "  @|bold helog log|@ @|yellow <host>|@",
                "  @|bold helog events|@ @|yellow <host>|@"
        },
        mixinStandardHelpOptions = true,
        versionProvider = Helog.VersionProvider.class
)
public final class Helog implements Callable<Integer> {

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec commandSpec;

    private enum Stream {
        LOG(new LogJsonStream()),
        EVENTS(new EventsJsonStream());

        private final JsonStream<?> jsonStream;

        Stream(JsonStream<?> jsonStream) {
            this.jsonStream = jsonStream;
        }
    }

    @Parameters(index = "0", hidden = true)
    private Stream stream;

    private String host;

    @Parameters(
            index = "1",
            description = "IP address or host name of the Hubitat Elevation. May optionally specify a port, with " +
                    "the format @|yellow <host>:<port>|@"
    )
    public void setHost(String host) {
        if (Strings.isHostPort(host)) {
            this.host = host;
        } else {
            throw new CommandLine.ParameterException(commandSpec.commandLine(), String.format(
                    "Invalid value '%s' for host: should be an IP address or hostname, optionally with a port using " +
                            "the format <host>:<port>", host));
        }
    }

    @Option(names = {"-r", "--raw"},
            description = "Write the stream exactly as received from the Hubitat Elevation.")
    public boolean raw;

    @Override
    public Integer call() throws URISyntaxException {
        URI uri = new URI("ws://" + host + "/" + stream.jsonStream.path());
        WebSocketSource source = new WebSocketSource(uri);

        if (raw) {
            new RawPrinter().run(source);
        } else {
            createJsonStreamPrinter(stream.jsonStream).run(source);
        }
        return -1;
    }

    private static <T> JsonStreamPrinter<T> createJsonStreamPrinter(JsonStream<T> jsonStream) {
        return new JsonStreamPrinter<>(jsonStream.type(), jsonStream.formatter());
    }

    public static int run(String... args) {
        return new CommandLine(new Helog()).setCaseInsensitiveEnumValuesAllowed(true).execute(args);
    }

    public static void main(String... args) {
        System.exit(run(args));
    }

    /**
     * Creates a version identifier for Picocli, based on information stored in the {@code MANIFEST.MF}.
     *
     * <p>The {@code Implementation-Title} and {@code Implementation-Version} properties are populated by
     * the {@code jar} section of {@code build.gradle}.
     */
    public static final class VersionProvider implements IVersionProvider {
        public String[] getVersion() {
            Package pack = Helog.class.getPackage();
            return new String[] {
                    pack.getImplementationTitle() + " " + pack.getImplementationVersion()
            };
        }
    }
}

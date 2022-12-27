package com.github.ianparkinson.helog;

import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

import static com.github.ianparkinson.helog.Strings.csvLine;

@CommandLine.Command(
        name = "helog",
        header = Helog.HEADER,
        synopsisHeading = "Usage:%n",
        customSynopsis = {
                "  @|bold helog log|@ @|yellow <host>|@",
                "  @|bold helog events|@ @|yellow <host>|@"
        },
        mixinStandardHelpOptions = true,
        versionProvider = Helog.VersionProvider.class
)
public final class Helog implements Callable<Integer> {

    static final String HEADER =
            "Writes live logs from a Hubitat Elevation's /logsocket or /eventsocket streams to stdout.";
    static final String ERROR_PREFIX = "Error: ";

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
            throw new ParameterException(commandSpec.commandLine(), String.format(
                    "Invalid value '%s' for host: should be an IP address or hostname, optionally with a port using " +
                            "the format <host>:<port>", host));
        }
    }

    @Option(names = "--device",
            description = "Writes logs for a specific device, specified using either the numeric id or the full " +
                    "device name (case sensitive).")
    public String device;

    @ArgGroup
    public Format format = new Format();

    static class Format {
        @Option(names = {"-r", "--raw"},
                description = "Write the stream exactly as received from the Hubitat Elevation.")
        public boolean raw;

        @Option(names = "--csv",
                description = "Render the stream in CSV format")
        public boolean csv;
    }

    @Option(names = "--kofi",
            description = "Buy the author a coffee.",
            help = true)
    public boolean kofi;

    @Override
    public Integer call() throws URISyntaxException {
        if (kofi) {
            kofi();
            return 0;
        }

        validateParameters();
        URI uri = new URI("ws://" + host + "/" + stream.jsonStream.path());
        WebSocketSource source = new WebSocketSource(Ansi.AUTO, uri);

        if (format.raw) {
            new RawPrinter(Ansi.AUTO).run(source);
        } else {
            createJsonStreamPrinter(stream.jsonStream).run(source);
        }
        return 1;
    }

    private void validateParameters() throws ParameterException {
        if (format.raw && device != null) {
            throw parameterException("--device cannot be used with --raw");
        }
    }

    private ParameterException parameterException(String message) {
        return new ParameterException(commandSpec.commandLine(), ERROR_PREFIX + message);
    }

    private <T> JsonStreamPrinter<T> createJsonStreamPrinter(JsonStream<T> jsonStream) {
        Predicate<T> filter = device != null ? jsonStream.device(device) : e -> true;
        if (format.csv) {
            return new JsonStreamPrinter<>(
                    Ansi.AUTO,
                    jsonStream.type(),
                    filter,
                    csvLine(jsonStream.csvHeader()),
                    jsonStream.csvFormatter().andThen(Strings::csvLine));
        } else {
            return new JsonStreamPrinter<>(Ansi.AUTO, jsonStream.type(), filter, null, jsonStream.formatter());
        }
    }

    public static void kofi() {
        System.out.println("Thank you!");
        System.out.println("https://ko-fi.com/ianparkinson");
    }

    public static int run(String... args) {
        if (args.length == 0) {
            // If called without any arguments, Picocli writes an error to stderr before writing usage. Bypass the
            // error, just print usage.
            CommandLine.usage(new Helog(), System.err);
            return 2;
        } else {
            return new CommandLine(new Helog())
                    .setCaseInsensitiveEnumValuesAllowed(true)
                    .setTrimQuotes(true)
                    .execute(args);
        }
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
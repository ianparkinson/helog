package com.github.ianparkinson.helog;

import com.github.ianparkinson.helog.app.JsonRenderer;
import com.github.ianparkinson.helog.app.JsonStream;
import com.github.ianparkinson.helog.app.StreamPrinter;
import com.github.ianparkinson.helog.app.TextWebSocketClient;
import com.github.ianparkinson.helog.app.TextWebSocketClientImpl;
import com.github.ianparkinson.helog.cli.FilterOptions;
import com.github.ianparkinson.helog.cli.FormatOptions;
import com.github.ianparkinson.helog.cli.ParameterValidationException;
import com.github.ianparkinson.helog.cli.Stream;
import com.github.ianparkinson.helog.util.Strings;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Help;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.ZoneId;
import java.util.concurrent.Callable;

import static com.github.ianparkinson.helog.util.Strings.csvLine;
import static java.lang.System.getProperty;

@CommandLine.Command(
        name = "helog",
        header = {Helog.HEADER, "", Helog.HOME_URL},
        synopsisHeading = "%nUsage:%n",
        customSynopsis = {
                "  @|bold helog log|@ @|yellow <host>|@",
                "  @|bold helog events|@ @|yellow <host>|@"
        },
        versionProvider = Helog.VersionProvider.class
)
public final class Helog implements Callable<Integer> {

    static final String HEADER =
            "Writes live logs from a Hubitat Elevation's /logsocket or /eventsocket streams to stdout.";
    static final String HOME_URL = "https://github.com/ianparkinson/helog";

    public static final String ERROR_PREFIX = "Error: ";

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec commandSpec;

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

    @ArgGroup(heading = "Output format:%n",
            exclusive = true)
    public FormatOptions format = new FormatOptions();

    @ArgGroup(heading = "Filters:%n",
            exclusive = false)
    public FilterOptions filter = new FilterOptions();

    @ArgGroup(heading = "Help:%n")
    public HelpOptions helpOptions = new HelpOptions();

    public static final class HelpOptions {
        @Option(names = { "-h", "--help" },
                usageHelp = true,
                description = "Show this help message and exit.")
        public boolean help;

        @Option(names = { "-v", "--version" },
                versionHelp = true,
                description = "Print version information and exit.")
        public boolean version;
    }

    @Override
    public Integer call() throws URISyntaxException, InterruptedException {
        try {
            filter.validate(stream, format);
        } catch (ParameterValidationException e) {
            throw new ParameterException(commandSpec.commandLine(), ERROR_PREFIX + e.getMessage());
        }

        TextWebSocketClient client = new TextWebSocketClientImpl();
        Clock clock = Clock.system(ZoneId.systemDefault());
        StreamPrinter printer = new StreamPrinter(clock, Ansi.AUTO, client);

        JsonStream<?> jsonStream = stream.jsonStream;
        URI uri = new URI("ws://" + host + "/" + stream.jsonStream.path());
        if (format.raw) {
            printer.stream(uri, null, (dateTime, text) -> text).waitUntilError();
        } else if (format.csv) {
            printer.stream(uri, csvLine(jsonStream.csvHeader()), createCsvJsonRenderer(jsonStream)).waitUntilError();
        } else {
            printer.stream(uri, null, createHumanReadableJsonRenderer(jsonStream)).waitUntilError();
        }
        return 1;
    }

    private <T> JsonRenderer<T> createHumanReadableJsonRenderer(JsonStream<T> jsonStream) {
        return new JsonRenderer<>(
                jsonStream.type(),
                filter.createPredicate(jsonStream),
                jsonStream.formatter());
    }

    private <T> JsonRenderer<T> createCsvJsonRenderer(JsonStream<T> jsonStream) {
        return new JsonRenderer<>(
                jsonStream.type(),
                filter.createPredicate(jsonStream),
                (dateTime, event) -> csvLine(jsonStream.csvFormatter().format(dateTime, event)));
    }

    public static int run(String... args) {
        CommandLine commandLine = new CommandLine(new Helog()).setTrimQuotes(true).setHelpFactory(new HelpFactory());

        if (args.length == 0) {
            // If called without any arguments, Picocli writes an error to stderr before writing usage. Bypass the
            // error, just print usage.
            commandLine.usage(System.err);
            return 2;
        } else {
            return commandLine.execute(args);
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
        @Override
        public String[] getVersion() {
            Package pack = Helog.class.getPackage();
            return new String[]{
                    pack.getImplementationTitle() + " " + pack.getImplementationVersion(),
                    "",
                    "JVM: " + getProperty("java.version") + " (" +
                        getProperty("java.vm.vendor") + " " + getProperty("java.vm.version") + ")",
                    "OS:  " + getProperty("os.name") + " " + getProperty("os.version") + " " + getProperty("os.arch")
            };
        }
    }

    /**
     * Customized renderer for help options, which tweaks the format for filter options.
     *
     * <p>Filter options can be used in either an "inclusive" or "exclusive" form; treated by Picocli as separate
     * options, but rendered as a single option with alternative forms to reduce duplication in the help text.
     * Where appropriate, this {@link HelpFactory} renders the option synopsis in the format:
     * <pre>
     *   --device=<device>[,<device>...], --xdevice=<device>[,<device>...]
     * </pre>
     */
    private static final class HelpFactory implements CommandLine.IHelpFactory {
        @Override
        public Help create(
                CommandLine.Model.CommandSpec commandSpec,
                Help.ColorScheme colorScheme) {
            return new Help(commandSpec, colorScheme) {
                @Override
                public IOptionRenderer createDefaultOptionRenderer() {
                    IOptionRenderer base = super.createDefaultOptionRenderer();
                    return (option, renderer, scheme) -> {
                        Ansi.Text[][] out = base.render(option, renderer, scheme);
                        if (option.names().length != 1) {
                            return out;
                        }

                        String exclusive = FilterOptions.getExclusiveAlternative(option.names()[0]);
                        if (exclusive != null) {
                            Ansi.Text paramLabelText = renderer.renderParameterLabel(
                                    option, scheme.ansi(), scheme.optionParamStyles());
                            out[0][3] = scheme.optionText(option.names()[0])
                                    .concat(paramLabelText)
                                    .concat(", ")
                                    .concat(scheme.optionText(exclusive))
                                    .concat(paramLabelText);
                        }
                        return out;
                    };
                }
            };
        }
    }
}
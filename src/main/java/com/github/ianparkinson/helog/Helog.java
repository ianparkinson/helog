package com.github.ianparkinson.helog;

import picocli.CommandLine;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Option;

import static picocli.CommandLine.ScopeType.INHERIT;

@CommandLine.Command(
        name = "helog",
        header = "Writes live logs from a Hubitat Evolution's /logsocket or /eventsocket streams to stdout.",
        synopsisHeading = "Usage:%n",
        customSynopsis = {
                "  @|bold helog log|@ @|yellow <host>|@",
                "  @|bold helog events|@ @|yellow <host>|@"
        },
        mixinStandardHelpOptions = true,
        subcommands = {HubitatLog.class, HubitatEvents.class},
        versionProvider = Helog.VersionProvider.class
)
public final class Helog {
    /**
     * Sets the subcommands to render the stream exactly as received from the Hubitat. Defined here so that it
     * appears in the global help options.
     */
    @Option(names = {"-r", "--raw"},
            description = "Write the stream exactly as received from the Hubitat Evolution.",
            scope = INHERIT)
    public boolean raw;

    public static int run(String... args) {
        return new CommandLine(new Helog()).execute(args);
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

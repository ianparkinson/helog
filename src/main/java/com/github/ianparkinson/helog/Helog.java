package com.github.ianparkinson.helog;

import picocli.CommandLine;
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
        version = {"helog 1.0.0-SNAPSHOT"},
        subcommands = {HubitatLog.class, HubitatEvents.class})
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
}

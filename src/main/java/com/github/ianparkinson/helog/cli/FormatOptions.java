package com.github.ianparkinson.helog.cli;

import picocli.CommandLine.Option;

/**
 * Encapsulates options controlling the output format.
 */
public final class FormatOptions {
    @Option(names = {"-r", "--raw"},
            description = "Write the stream exactly as received from the Hubitat Elevation.")
    public boolean raw;

    @Option(names = "--csv",
            description = "Render the stream in CSV format")
    public boolean csv;
}

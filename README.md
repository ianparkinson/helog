# `helog`

`helog` is a command-line utiltiy which connects to a [Hubitat Elevation](https://hubitat.com/) and writes the event stream, or debug log, to stdout.

## Installation

1. You'll need a Java Runtime Environment installed, version 11 or later.
1. Download and unpack an archive from the [releases](https://github.com/ianparkinson/helog/releases) page.

The `helog` command executable can be found inside the `bin` directory of the unpacked archive.

## Usage

To display the Eleveation's debug log, find your Elevation's IP address (I'll assume it's 192.168.1.200) and run:

`helog log 192.168.1.200`

To display the events stream, run:

`helog events 192.168.1.200`

More information about the available command-line options is available with:

`helog --help`

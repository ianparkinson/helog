# `helog`

`helog` is a command-line utility which connects to a [Hubitat Elevation](https://hubitat.com/) and writes the live event stream, or debug log, to stdout.

[![Tests](https://github.com/ianparkinson/helog/actions/workflows/check.yml/badge.svg)](https://github.com/ianparkinson/helog/actions/workflows/check.yml?event=push)

* [Installation](#installation)
* [Usage](#usage)
* [Debug log](#debug-log)
  * [CSV output](#csv-output)
  * [Raw output](#raw-output)
  * [Filtering](#filtering)
* [Event log](#event-log)
    * [CSV output](#csv-output-1)
    * [Raw output](#raw-output-1)
    * [Filtering](#filtering-1)

## Installation

1. You'll need a Java Runtime Environment installed, version 11 or later.
1. Download and unpack an archive from the [releases](https://github.com/ianparkinson/helog/releases) page.

The `helog` command executable can be found inside the `bin` directory of the unpacked archive.

## Usage

To display the Elevation's debug log, find your Elevation's IP address (I'll assume it's 192.168.1.200) and run:

`helog log 192.168.1.200`

To display the events stream, run:

`helog events 192.168.1.200`

More information about the available command-line options is available with:

`helog --help`

## Debug log

By default, `helog` writes debug events in a human-readable format, for example:

```
> helog log 192.168.1.200
Connected to ws://192.168.1.200/logsocket
2023-04-08T12:58:15.580+01:00 info   dev 36 Underfloor Heating  Underfloor Heating temperature 20 C
2023-04-08T12:59:05.935+01:00 trace  dev 36 Underfloor Heating  Application ID Received
```

From left-to-right, the fields are:

1. The time at which the event was received by `helog`, using the local clock.
2. The log level: `error`, `warn`, `info`, `debug`, or `trace`.
3. The source type, either `dev` (for a device) or `app` (for an app).
4. The numeric id of the source device or app.
5. The name of the source device or app.
6. The log message text.

### CSV output

`helog` can emit CSV output, useful for importing into a spreadsheet, using the `--csv` flag.

```
./helog log 192.168.1.200 --csv
Connected to ws://192.168.1.200/logsocket
localTime,name,msg,id,time,type,level
2023-04-08T12:58:15.580+01:00,Underfloor Heating,Underfloor Heating temperature 20 C,36,2023-04-08 12:58:16.416,dev,info
2023-04-08T12:59:05.935+01:00,Underfloor Heating,Application ID Received,36,2023-04-08 12:59:06.740,dev,trace
```

The fields are:
* `localTime` - The time at which the event was received by `helog`, using the local clock.
* `name` - The name of the source device or app.
* `msg` - The log message text
* `id` - The numeric id of the source device or app.
* `time` - The time at which the event was emitted, using the Hubitat Elevation's clock.
* `type` - The source type, either `dev` (for a device) or `app` (for an app).
* `level` - The log level: `error`, `warn`, `info`, `debug`, or `trace`.

Most of these fields correspond to columns in the Hubitat Elevation's [logs](
https://docs2.hubitat.com/en/user-interface/advanced-features/logs) view.

### Raw output

If `--raw` is specified, Helog will write the events exactly as they are received in JSON format, with a newline
between events:

```
> helog log 192.168.1.200 --raw
Connected to ws://192.168.1.200/logsocket
{"name":"Underfloor Heating","msg":"Underfloor Heating temperature 20 C","id":36,"time":"2023-04-08 12:58:16.416","type":"dev","level":"info"}
{"name":"Underfloor Heating","msg":"Application ID Received","id":36,"time":"2023-04-08 12:59:06.740","type":"dev","level":"trace"}
```

### Filtering

The debug log can be filtered to only include events from specific devices or apps with the `--device` or `--app`
options. Devices and apps can be specified by either their numeric id or their name, for example:

```
> helog log 192.168.1.200 --device=34,36 --app="Hubitat Package Manager"
```

...will only display log events emitted from devices with Ids 34 and 36, and from the Package Manager. The `--xdevice`
and `--xapp` options have the opposite effect: they include all log events except for those from the
specified sources.

The debug log can also be filtered using `--level` or `--xlevel` to include (or exclude) log entries from the given
level. For example, to see just `error` or `warn` events, use:

```
> helog log 192.168.1.200 --level=error,warn
```

## Event log

By default, `helog` writes the event log in a human-readable format, for example:

```
> helog events 192.168.1.200
Connected to ws://192.168.1.200/eventsocket
2023-04-08T14:06:03.916+01:00 DEVICE 36 Underfloor Heating: temperature 20 C
2023-04-08T14:06:33.670+01:00 DEVICE 34 Lamp: switch on
```

From left-to-right, the fields are:

1. The time at which the event was received by helog, using the local clock.
2. The source type, either `DEVICE` (for a device) or `APP` (for an app).
3. The numeric id of the source.
4. The name of the source device. The name is not available if the source is an app.
5. The name of the event (in the example above, `temperature` or `switch`)
6. The value associated with the event (`20` or `on`)
7. If available, the units associated with the event value.
8. If available, a textual description of the event.

### CSV output

`helog` can emit CSV output, useful for importing into a spreadsheet, using the `--csv` flag.

```
> helog events 192.168.1.200 --csv
Connected to ws://192.168.1.200/eventsocket
localTime,source,name,displayName,value,type,unit,deviceId,hubId,installedAppId,descriptionText
2023-04-08T14:06:03.918+01:00,DEVICE,temperature,Bathroom Underfloor Heating,20,,C,36,0,0,
2023-04-08T14:06:33.670+01:00,DEVICE,switch,Lamp,on,digital,,34,0,0,
```

The fields are:
* `localTime` - The time at which the event was received by `helog`, using the local clock.
* `source` - The source type, either `DEVICE` or `APP`.
* `name` - The name of the event.
* `displayName` - The name of the source device. Not available for an app.
* `value` - The value associated with the event.
* `type` - If available, the event type.
* `unit` - If available, the units associated with the event value.
* `deviceId` - The numeric id of the source device. Zero if the event was emitted by an app.
* `hubId` - A numeric id identifying the hub, typically zero.
* `installedAppId` - The numeric id of the source app. Zero if the event was emitted by a device.
* `descriptionText` - If available, a textual description of the event.

Most of these fields correspond to columns in the Hubitat Elevation's [Device Events](
https://docs2.hubitat.com/user-interface/devices/device-events) view.

### Raw output

If `--raw` is specified, Helog will write the events exactly as they are received in JSON format, with a newline
between events:

```
> helog events 192.168.1.200 --raw
Connected to ws://192.168.1.200/eventsocket
{ "source":"DEVICE","name":"temperature","displayName" : "Underfloor Heating", "value" : "20", "type" : "null", "unit":"C","deviceId":36,"hubId":0,"installedAppId":0,"descriptionText" : "null"}
{ "source":"DEVICE","name":"switch","displayName" : "Lamp", "value" : "on", "type" : "digital", "unit":"null","deviceId":34,"hubId":0,"installedAppId":0,"descriptionText" : "null"}
```

### Filtering

The event log can be filtered to only include events from specific devices or apps with the `--device` or `--app`
options. Devices can be specified by either their numeric id or their name, but apps can be specified only with
their numeric id. For example:

```
> helog events 192.168.1.200 --device=Lamp,36"
```

...will only display log events emitted from the device with the name `Lamp` and from the device with id 36.
The `--xdevice` and `--xapp` options have the opposite effect: they include all log events except for those from
the specified sources.

The event log can also be filtered using `--name` or `--xname` to include (or exclude) events with the given
name. For example, to see just `temperature` events, you could use:

```
> helog events 192.168.1.200 --name=temperature
```
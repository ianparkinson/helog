package com.github.ianparkinson.helog.cli;

import com.github.ianparkinson.helog.app.JsonStream;
import com.github.ianparkinson.helog.util.Strings;
import picocli.CommandLine.Option;

import java.util.List;
import java.util.function.Predicate;

/**
 * Encapsulates options allowing the logged events to be filtered.
 */
public class FilterOptions {
    @Option(names = "--device",
            split = ",",
            description = "Writes logs for specific devices, specified using either the numeric id or the full " +
                    "device name (case sensitive).")
    public List<String> device;

    @Option(names = "--xdevice",
            split = ",",
            description = "Exclude specific devices, specified using either the numeric id or the full " +
                    "device name (case sensitive).",
            paramLabel = "device")
    public List<String> excludeDevice;

    @Option(names = "--app",
            split = ",",
            description = "Writes logs for specific apps, specified using the numeric id. If used with " +
                    "@|bold log|@, the app name (case sensitive) can also be used.")
    public List<String> app;

    @Option(names = "--xapp",
            split = ",",
            description = "Exclude specific apps, specified using the numeric id. If used with " +
                    "@|bold log|@, the app name (case sensitive) can also be used.",
            paramLabel = "app")
    public List<String> excludeApp;

    public void validate(Stream stream, FormatOptions formatOptions) throws ParameterValidationException {
        if (formatOptions.raw) {
            if (device != null) {
                throw new ParameterValidationException("--device cannot be used with --raw");
            }
            if (excludeDevice != null) {
                throw new ParameterValidationException("--xdevice cannot be used with --raw");
            }
            if (app != null) {
                throw new ParameterValidationException("--app cannot be used with --raw");
            }
            if (excludeApp != null) {
                throw new ParameterValidationException("--xapp cannot be used with --raw");
            }
        }

        if ((device != null || app != null) && (excludeDevice != null || excludeApp != null)) {
            throw new ParameterValidationException("--device or --app cannot be used with --xdevice or --xapp");
        }

        if (stream == Stream.EVENTS
                && (!stream(app).allMatch(Strings::isInteger) || !stream(excludeApp).allMatch(Strings::isInteger))) {
            throw new ParameterValidationException(
                    "Events cannot be filtered by app name. Use the numeric id instead.");
        }
    }

    public <T> Predicate<T> createPredicate(JsonStream<T> jsonStream) {
        if (!(isNullOrEmpty(device) && isNullOrEmpty(app))) {
            Predicate<T> devicePredicate = stream(device).map(jsonStream::device).reduce(e -> false, Predicate::or);
            Predicate<T> appPredicate = stream(app).map(jsonStream::app).reduce(e -> false, Predicate::or);
            return devicePredicate.or(appPredicate);
        } else if (!(isNullOrEmpty(excludeDevice) && isNullOrEmpty(excludeApp))) {
            Predicate<T> devicePredicate = stream(excludeDevice)
                    .map(jsonStream::device)
                    .map(Predicate::not)
                    .reduce(e -> true, Predicate::and);
            Predicate<T> appPredicate = stream(excludeApp)
                    .map(jsonStream::app)
                    .map(Predicate::not)
                    .reduce(e -> true, Predicate::and);
            return devicePredicate.and(appPredicate);
        } else {
            return e -> true;
        }
    }

    private <T> java.util.stream.Stream<T> stream(List<T> list) {
        return (list == null) ? java.util.stream.Stream.empty() : list.stream();
    }

    private <T> boolean isNullOrEmpty(List<T> list) {
        return (list == null || list.isEmpty());
    }
}

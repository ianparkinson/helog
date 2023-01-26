package com.github.ianparkinson.helog.cli;

import com.github.ianparkinson.helog.app.JsonStream;
import com.github.ianparkinson.helog.util.Strings;
import picocli.CommandLine.Option;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Encapsulates options allowing the logged events to be filtered.
 */
public class FilterOptions {
    @Option(names = "--device",
            split = ",",
            description = "Include (or exclude) specific devices, specified using either the numeric id or the full " +
                    "device name (case sensitive).")
    public List<String> device;

    @Option(names = "--xdevice",
            split = ",",
            hidden = true)
    public List<String> excludeDevice;

    @Option(names = "--app",
            split = ",",
            description = "Include (or exclude) specific apps, specified using the numeric id. If used with " +
                    "@|bold log|@, the app name (case sensitive) can also be used.")
    public List<String> app;

    @Option(names = "--xapp",
            split = ",",
            hidden = true)
    public List<String> excludeApp;

    @Option(names = "--name",
            split = ",",
            description = "Include (or exclude) events with the given name. Case sensitive.")
    public List<String> name;

    @Option(names = "--xname",
            split = ",",
            hidden = true)
    public List<String> excludeName;

    @Option(names = "--level",
            split = ",",
            description = "Include (or exclude) log entries with the given level: @|bold error|@, @|bold warn|@," +
                    "@|bold info|@, @|bold debug|@ or @|bold trace|@.")
    public List<LogLevel> level;

    @Option(names = "--xlevel",
            split = ",",
            hidden = true)
    public List<LogLevel> excludeLevel;

    public enum LogLevel {
        // Lower-case for use as command-line options
        error, warn, info, debug, trace
    }

    public void validate(Stream stream, FormatOptions formatOptions) throws ParameterValidationException {
        if (formatOptions.raw) {
            enforce(device == null, "--device cannot be used with --raw");
            enforce(excludeDevice == null, "--xdevice cannot be used with --raw");
            enforce(app == null, "--app cannot be used with --raw");
            enforce(excludeApp == null, "--xapp cannot be used with --raw");
            enforce(name == null, "--name cannot be used with --raw");
            enforce(excludeName == null, "--xname cannot be used with --raw");
            enforce(level == null, "--level cannot be used with --raw");
            enforce(excludeLevel == null, "--xlevel cannot be used with --raw");
        }

        boolean sourceFilteredInclusively = device != null || app != null;
        boolean sourceFilteredExclusively = excludeDevice != null || excludeApp != null;
        enforce(!(sourceFilteredInclusively && sourceFilteredExclusively),
                "--device or --app cannot be used with --xdevice or --xapp");
        enforce(!(name != null && excludeName != null), "--name and --xname cannot be used together");
        enforce(!(level != null && excludeLevel != null), "--level and --xlevel cannot be used together");

        if (stream == Stream.log) {
            enforce(name == null, "--name cannot be used with log");
            enforce(excludeName == null, "--xname cannot be used with log");
        }

        if (stream == Stream.events) {
            enforce(stream(app).allMatch(Strings::isInteger),
                    "Events cannot be filtered by app name. Use the numeric id instead.");
            enforce(stream(excludeApp).allMatch(Strings::isInteger),
                    "Events cannot be filtered by app name. Use the numeric id instead.");
            enforce(level == null, "--level cannot be used with events");
            enforce(excludeLevel == null, "--xlevel cannot be used with events");
        }
    }

    private void enforce(boolean condition, String message) throws ParameterValidationException {
        if (!condition) {
            throw new ParameterValidationException(message);
        }
    }

    public <T> Predicate<T> createPredicate(JsonStream<T> jsonStream) {
        return createSourcePredicate(jsonStream)
                .and(createNamePredicate(jsonStream))
                .and(createLevelPredicate(jsonStream));
    }

    private <T> Predicate<T> createSourcePredicate(JsonStream<T> jsonStream) {
        if (!(isNullOrEmpty(device) && isNullOrEmpty(app))) {
            Predicate<T> devicePredicate = anyOf(device, jsonStream::device);
            Predicate<T> appPredicate = anyOf(app, jsonStream::app);
            return devicePredicate.or(appPredicate);
        } else {
            Predicate<T> devicePredicate = noneOf(excludeDevice, jsonStream::device);
            Predicate<T> appPredicate = noneOf(excludeApp, jsonStream::app);
            return devicePredicate.and(appPredicate);
        }
    }

    private <T> Predicate<T> createNamePredicate(JsonStream<T> jsonStream) {
        if (!isNullOrEmpty(name)) {
            return anyOf(name, jsonStream::eventName);
        } else {
            return noneOf(excludeName, jsonStream::eventName);
        }
    }

    private <T> Predicate<T> createLevelPredicate(JsonStream<T> jsonStream) {
        if (!isNullOrEmpty(level)) {
            return anyOf(level, logLevel -> jsonStream.logLevel(logLevel.name()));
        } else {
            return noneOf(excludeLevel, logLevel -> jsonStream.logLevel(logLevel.name()));
        }
    }

    /**
     * Builds a {@link Predicate} which selects for objects with some property matching any of the given {@code values}.
     *
     * @param values            Values to match.
     * @param predicateFunction Builds a {@link Predicate} testing whether the object has a property matching an
     *                          individual value.
     * @param <V>               Type of values.
     * @param <T>               Type of object to be tested by the resulting {@link Predicate}.
     */
    private <V, T> Predicate<T> anyOf(List<V> values, Function<V, Predicate<T>> predicateFunction) {
        return stream(values).map(predicateFunction).reduce(e -> false, Predicate::or);
    }

    /**
     * Builds a {@link Predicate} which selects for objects with some property matching none of the given {@code
     * values}.
     *
     * @param values            Values to match.
     * @param predicateFunction Builds a {@link Predicate} testing whether the object has a property matching an
     *                          individual value.
     * @param <V>               Type of values.
     * @param <T>               Type of object to be tested by the resulting {@link Predicate}.
     */
    private <V, T> Predicate<T> noneOf(List<V> values, Function<V, Predicate<T>> predicateFunction) {
        return stream(values).map(predicateFunction).map(Predicate::not).reduce(e -> true, Predicate::and);
    }

    private <T> java.util.stream.Stream<T> stream(List<T> list) {
        return (list == null) ? java.util.stream.Stream.empty() : list.stream();
    }

    private <T> boolean isNullOrEmpty(List<T> list) {
        return (list == null || list.isEmpty());
    }

    public static String getExclusiveAlternative(String name) {
        switch (name) {
            case "--app":
                return "--xapp";
            case "--device":
                return "--xdevice";
            case "--level":
                return "--xlevel";
            case "--name":
                return "--xname";
            default:
                return null;
        }
    }
}

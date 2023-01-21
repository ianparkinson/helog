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

    @Option(names = "--name",
            split = ",",
            description = "Include events with the given name. Case sensitive.")
    public List<String> name;

    @Option(names = "--xname",
            split = ",",
            description = "Exclude events with the given name. Case sensitive.")
    public List<String> excludeName;

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
            if (name != null) {
                throw new ParameterValidationException("--name cannot be used with --raw");
            }
            if (excludeName != null) {
                throw new ParameterValidationException("--xname cannot be used with --raw");
            }
        }

        if ((device != null || app != null) && (excludeDevice != null || excludeApp != null)) {
            throw new ParameterValidationException("--device or --app cannot be used with --xdevice or --xapp");
        }

        if (name != null && excludeName != null) {
            throw new ParameterValidationException("--name and --xname cannot be used together");
        }

        if (stream == Stream.LOG) {
            if (name != null) {
                throw new ParameterValidationException("--name cannot be used with log");
            }
            if (excludeName != null) {
                throw new ParameterValidationException("--xname cannot be used with log");
            }
        }

        if (stream == Stream.EVENTS
                && (!stream(app).allMatch(Strings::isInteger) || !stream(excludeApp).allMatch(Strings::isInteger))) {
            throw new ParameterValidationException(
                    "Events cannot be filtered by app name. Use the numeric id instead.");
        }
    }

    public <T> Predicate<T> createPredicate(JsonStream<T> jsonStream) {
        return createSourcePredicate(jsonStream)
                .and(createNamePredicate(jsonStream));
    }

    public <T> Predicate<T> createSourcePredicate(JsonStream<T> jsonStream) {
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

    public <T> Predicate<T> createNamePredicate(JsonStream<T> jsonStream) {
        if (!isNullOrEmpty(name)) {
            return anyOf(name, jsonStream::eventName);
        } else {
            return noneOf(excludeName, jsonStream::eventName);
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
}

package com.github.ianparkinson.helog.cli;

import com.github.ianparkinson.helog.app.JsonStream;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FilterOptionsTest {
    private static final TestEntry ENTRY_A = new TestEntry("deviceA", "appA", "nameA");
    private static final TestEntry ENTRY_B = new TestEntry("deviceB", "appB", "nameB");
    private static final TestEntry ENTRY_C = new TestEntry("deviceC", "appC", "nameC");

    private final FilterOptions filterOptions = new FilterOptions();
    private final FormatOptions formatOptions = new FormatOptions();
    private final TestJsonStream stream = new TestJsonStream();

    @Test
    public void validate_rawDisallowsDevice() {
        formatOptions.raw = true;
        filterOptions.device = List.of("42");
        assertThrows(ParameterValidationException.class, () -> filterOptions.validate(Stream.LOG, formatOptions));
    }

    @Test
    public void validate_rawDisallowsExcludeDevice() {
        formatOptions.raw = true;
        filterOptions.excludeDevice = List.of("42");
        assertThrows(ParameterValidationException.class, () -> filterOptions.validate(Stream.LOG, formatOptions));
    }

    @Test
    public void validate_rawDisallowsApp() {
        formatOptions.raw = true;
        filterOptions.app = List.of("42");
        assertThrows(ParameterValidationException.class, () -> filterOptions.validate(Stream.LOG, formatOptions));
    }

    @Test
    public void validate_rawDisallowsExcludeApp() {
        formatOptions.raw = true;
        filterOptions.excludeApp = List.of("42");
        assertThrows(ParameterValidationException.class, () -> filterOptions.validate(Stream.LOG, formatOptions));
    }

    @Test
    public void validate_rawDisallowsName() {
        formatOptions.raw = true;
        filterOptions.name = List.of("name");
        assertThrows(ParameterValidationException.class, () -> filterOptions.validate(Stream.EVENTS, formatOptions));
    }

    @Test
    public void validate_rawDisallowsExcludeName() {
        formatOptions.raw = true;
        filterOptions.excludeName = List.of("name");
        assertThrows(ParameterValidationException.class, () -> filterOptions.validate(Stream.EVENTS, formatOptions));
    }

    @Test
    public void validate_csvAllowsInclusiveSourceFilters() {
        formatOptions.csv = true;
        filterOptions.app = List.of("42");
        filterOptions.device = List.of("42");
        assertDoesNotThrow(() -> filterOptions.validate(Stream.LOG, formatOptions));
    }

    @Test
    public void validate_csvAllowsExclusiveSourceFilters() {
        formatOptions.csv = true;
        filterOptions.excludeApp = List.of("42");
        filterOptions.excludeDevice = List.of("42");
        assertDoesNotThrow(() -> filterOptions.validate(Stream.LOG, formatOptions));
    }

    @Test
    public void validate_csvAllowsInclusiveNameFilter() {
        formatOptions.csv = true;
        filterOptions.name = List.of("name");
        assertDoesNotThrow(() -> filterOptions.validate(Stream.EVENTS, formatOptions));
    }

    @Test
    public void validate_csvAllowsExclusiveNameFilter() {
        formatOptions.csv = true;
        filterOptions.excludeName = List.of("name");
        assertDoesNotThrow(() -> filterOptions.validate(Stream.EVENTS, formatOptions));
    }

    @Test
    public void validate_logAllowsInclusiveSourceFiltersByNumber() {
        filterOptions.app = List.of("42");
        filterOptions.device = List.of("42");
        assertDoesNotThrow(() -> filterOptions.validate(Stream.LOG, formatOptions));
    }

    @Test
    public void validate_logAllowsExclusiveSourceFiltersByNumber() {
        filterOptions.excludeApp = List.of("42");
        filterOptions.excludeDevice = List.of("42");
        assertDoesNotThrow(() -> filterOptions.validate(Stream.LOG, formatOptions));
    }

    @Test
    public void validate_logDisallowsName() {
        filterOptions.name = List.of("name");
        assertThrows(ParameterValidationException.class, () -> filterOptions.validate(Stream.LOG, formatOptions));
    }

    @Test
    public void validate_logDisallowsExcludeName() {
        filterOptions.excludeName = List.of("name");
        assertThrows(ParameterValidationException.class, () -> filterOptions.validate(Stream.LOG, formatOptions));
    }

    @Test
    public void validate_eventsAllowsInclusiveSourceFiltersByNumber() {
        filterOptions.app = List.of("42");
        filterOptions.device = List.of("42");
        assertDoesNotThrow(() -> filterOptions.validate(Stream.EVENTS, formatOptions));
    }

    @Test
    public void validate_eventsAllowsExclusiveSourceFiltersByNumber() {
        filterOptions.excludeApp = List.of("42");
        filterOptions.excludeDevice = List.of("42");
        assertDoesNotThrow(() -> filterOptions.validate(Stream.EVENTS, formatOptions));
    }

    @Test
    public void validate_eventsAllowsInclusiveNameFilter() {
        filterOptions.name = List.of("name");
        assertDoesNotThrow(() -> filterOptions.validate(Stream.EVENTS, formatOptions));
    }

    @Test
    public void validate_eventsAllowsExclusiveNameFilter() {
        filterOptions.excludeName = List.of("name");
        assertDoesNotThrow(() -> filterOptions.validate(Stream.EVENTS, formatOptions));
    }

    @Test
    public void validate_disallowsInclusiveAndExclusiveDeviceFilters() {
        filterOptions.device = List.of("42");
        filterOptions.excludeDevice = List.of("43");
        assertThrows(ParameterValidationException.class, () -> filterOptions.validate(Stream.LOG, formatOptions));
    }

    @Test
    public void validate_disallowsInclusiveAndExclusiveAppFilters() {
        filterOptions.app = List.of("42");
        filterOptions.excludeApp = List.of("43");
        assertThrows(ParameterValidationException.class, () -> filterOptions.validate(Stream.LOG, formatOptions));
    }

    @Test
    public void validate_disallowsInclusiveDeviceAndExclusiveAppFilters() {
        filterOptions.device = List.of("42");
        filterOptions.excludeApp = List.of("43");
        assertThrows(ParameterValidationException.class, () -> filterOptions.validate(Stream.LOG, formatOptions));
    }

    @Test
    public void validate_disallowsExclusiveDeviceAndInclusiveAppFilters() {
        filterOptions.excludeDevice = List.of("42");
        filterOptions.app = List.of("43");
        assertThrows(ParameterValidationException.class, () -> filterOptions.validate(Stream.LOG, formatOptions));
    }

    @Test
    public void validate_logAllowsInclusiveAppName() {
        filterOptions.app = List.of("name");
        assertDoesNotThrow(() -> filterOptions.validate(Stream.LOG, formatOptions));
    }

    @Test
    public void validate_logAllowsExclusiveAppName() {
        filterOptions.excludeApp = List.of("name");
        assertDoesNotThrow(() -> filterOptions.validate(Stream.LOG, formatOptions));
    }

    @Test
    public void validate_logAllowsInclusiveDeviceName() {
        filterOptions.device = List.of("name");
        assertDoesNotThrow(() -> filterOptions.validate(Stream.LOG, formatOptions));
    }

    @Test
    public void validate_logAllowsExclusiveDeviceName() {
        filterOptions.excludeDevice = List.of("name");
        assertDoesNotThrow(() -> filterOptions.validate(Stream.LOG, formatOptions));
    }

    @Test
    public void validate_eventsDisallowsInclusiveAppName() {
        filterOptions.app = List.of("name");
        assertThrows(ParameterValidationException.class, () -> filterOptions.validate(Stream.EVENTS, formatOptions));
    }

    @Test
    public void validate_eventsDisallowsExclusiveAppName() {
        filterOptions.excludeApp = List.of("name");
        assertThrows(ParameterValidationException.class, () -> filterOptions.validate(Stream.EVENTS, formatOptions));
    }

    @Test
    public void validate_eventsAllowsInclusiveDeviceName() {
        filterOptions.device = List.of("name");
        assertDoesNotThrow(() -> filterOptions.validate(Stream.EVENTS, formatOptions));
    }

    @Test
    public void validate_eventsAllowsExclusiveDeviceName() {
        filterOptions.excludeDevice = List.of("name");
        assertDoesNotThrow(() -> filterOptions.validate(Stream.EVENTS, formatOptions));
    }

    @Test
    public void validate_disallowsInclusiveDeviceAndExclusiveNameFilters() {
        filterOptions.name = List.of("name1");
        filterOptions.excludeName = List.of("name2");
        assertThrows(ParameterValidationException.class, () -> filterOptions.validate(Stream.EVENTS, formatOptions));
    }

    @Test
    public void createPredicate_noFilters() {
        Predicate<TestEntry> predicate = filterOptions.createPredicate(stream);
        assertThat(predicate.test(ENTRY_A)).isTrue();
        assertThat(predicate.test(ENTRY_B)).isTrue();
        assertThat(predicate.test(ENTRY_C)).isTrue();
    }

    @Test
    public void createPredicate_device() {
        filterOptions.device = List.of("deviceB");
        Predicate<TestEntry> predicate = filterOptions.createPredicate(stream);
        assertThat(predicate.test(ENTRY_A)).isFalse();
        assertThat(predicate.test(ENTRY_B)).isTrue();
        assertThat(predicate.test(ENTRY_C)).isFalse();
    }

    @Test
    public void createPredicate_multipleDevices() {
        filterOptions.device = List.of("deviceA", "deviceC");
        Predicate<TestEntry> predicate = filterOptions.createPredicate(stream);
        assertThat(predicate.test(ENTRY_A)).isTrue();
        assertThat(predicate.test(ENTRY_B)).isFalse();
        assertThat(predicate.test(ENTRY_C)).isTrue();
    }

    @Test
    public void createPredicate_excludeDevice() {
        filterOptions.excludeDevice = List.of("deviceB");
        Predicate<TestEntry> predicate = filterOptions.createPredicate(stream);
        assertThat(predicate.test(ENTRY_A)).isTrue();
        assertThat(predicate.test(ENTRY_B)).isFalse();
        assertThat(predicate.test(ENTRY_C)).isTrue();
    }

    @Test
    public void createPredicate_excludeMultipleDevices() {
        filterOptions.excludeDevice = List.of("deviceA", "deviceC");
        Predicate<TestEntry> predicate = filterOptions.createPredicate(stream);
        assertThat(predicate.test(ENTRY_A)).isFalse();
        assertThat(predicate.test(ENTRY_B)).isTrue();
        assertThat(predicate.test(ENTRY_C)).isFalse();
    }

    @Test
    public void createPredicate_app() {
        filterOptions.app = List.of("appB");
        Predicate<TestEntry> predicate = filterOptions.createPredicate(stream);
        assertThat(predicate.test(ENTRY_A)).isFalse();
        assertThat(predicate.test(ENTRY_B)).isTrue();
        assertThat(predicate.test(ENTRY_C)).isFalse();
    }

    @Test
    public void createPredicate_multipleApps() {
        filterOptions.app = List.of("appA", "appC");
        Predicate<TestEntry> predicate = filterOptions.createPredicate(stream);
        assertThat(predicate.test(ENTRY_A)).isTrue();
        assertThat(predicate.test(ENTRY_B)).isFalse();
        assertThat(predicate.test(ENTRY_C)).isTrue();
    }

    @Test
    public void createPredicate_excludeApp() {
        filterOptions.excludeApp = List.of("appB");
        Predicate<TestEntry> predicate = filterOptions.createPredicate(stream);
        assertThat(predicate.test(ENTRY_A)).isTrue();
        assertThat(predicate.test(ENTRY_B)).isFalse();
        assertThat(predicate.test(ENTRY_C)).isTrue();
    }

    @Test
    public void createPredicate_excludeMultipleApps() {
        filterOptions.excludeApp = List.of("appA", "appC");
        Predicate<TestEntry> predicate = filterOptions.createPredicate(stream);
        assertThat(predicate.test(ENTRY_A)).isFalse();
        assertThat(predicate.test(ENTRY_B)).isTrue();
        assertThat(predicate.test(ENTRY_C)).isFalse();
    }

    @Test
    public void createPredicate_includeDeviceAndApp() {
        filterOptions.app = List.of("appA");
        filterOptions.device = List.of("deviceC");
        Predicate<TestEntry> predicate = filterOptions.createPredicate(stream);
        assertThat(predicate.test(ENTRY_A)).isTrue();
        assertThat(predicate.test(ENTRY_B)).isFalse();
        assertThat(predicate.test(ENTRY_C)).isTrue();
    }

    @Test
    public void createPredicate_excludeDeviceAndApp() {
        filterOptions.excludeApp = List.of("appA");
        filterOptions.excludeDevice = List.of("deviceC");
        Predicate<TestEntry> predicate = filterOptions.createPredicate(stream);
        assertThat(predicate.test(ENTRY_A)).isFalse();
        assertThat(predicate.test(ENTRY_B)).isTrue();
        assertThat(predicate.test(ENTRY_C)).isFalse();
    }

    @Test
    public void createPredicate_name() {
        filterOptions.name = List.of("nameB");
        Predicate<TestEntry> predicate = filterOptions.createPredicate(stream);
        assertThat(predicate.test(ENTRY_A)).isFalse();
        assertThat(predicate.test(ENTRY_B)).isTrue();
        assertThat(predicate.test(ENTRY_C)).isFalse();
    }

    @Test
    public void createPredicate_multipleNames() {
        filterOptions.name = List.of("nameA", "nameC");
        Predicate<TestEntry> predicate = filterOptions.createPredicate(stream);
        assertThat(predicate.test(ENTRY_A)).isTrue();
        assertThat(predicate.test(ENTRY_B)).isFalse();
        assertThat(predicate.test(ENTRY_C)).isTrue();
    }

    @Test
    public void createPredicate_excludeName() {
        filterOptions.excludeName = List.of("nameB");
        Predicate<TestEntry> predicate = filterOptions.createPredicate(stream);
        assertThat(predicate.test(ENTRY_A)).isTrue();
        assertThat(predicate.test(ENTRY_B)).isFalse();
        assertThat(predicate.test(ENTRY_C)).isTrue();
    }

    @Test
    public void createPredicate_excludeMultipleNames() {
        filterOptions.excludeName = List.of("nameA", "nameC");
        Predicate<TestEntry> predicate = filterOptions.createPredicate(stream);
        assertThat(predicate.test(ENTRY_A)).isFalse();
        assertThat(predicate.test(ENTRY_B)).isTrue();
        assertThat(predicate.test(ENTRY_C)).isFalse();
    }

    @Test
    public void createPredicate_includeSourceIncludeName() {
        filterOptions.device = List.of("deviceA", "deviceB");
        filterOptions.name = List.of("nameB", "nameC");
        Predicate<TestEntry> predicate = filterOptions.createPredicate(stream);
        assertThat(predicate.test(ENTRY_A)).isFalse();
        assertThat(predicate.test(ENTRY_B)).isTrue();
        assertThat(predicate.test(ENTRY_C)).isFalse();
    }

    @Test
    public void createPredicate_includeSourceExcludeName() {
        filterOptions.device = List.of("deviceA", "deviceB");
        filterOptions.excludeName = List.of("nameB", "nameC");
        Predicate<TestEntry> predicate = filterOptions.createPredicate(stream);
        assertThat(predicate.test(ENTRY_A)).isTrue();
        assertThat(predicate.test(ENTRY_B)).isFalse();
        assertThat(predicate.test(ENTRY_C)).isFalse();
    }

    @Test
    public void createPredicate_excludeSourceIncludeName() {
        filterOptions.excludeDevice = List.of("deviceA", "deviceB");
        filterOptions.name = List.of("nameB", "nameC");
        Predicate<TestEntry> predicate = filterOptions.createPredicate(stream);
        assertThat(predicate.test(ENTRY_A)).isFalse();
        assertThat(predicate.test(ENTRY_B)).isFalse();
        assertThat(predicate.test(ENTRY_C)).isTrue();
    }

    @Test
    public void createPredicate_excludeSourceExcludeName() {
        filterOptions.excludeDevice = List.of("deviceA");
        filterOptions.excludeName = List.of("nameC");
        Predicate<TestEntry> predicate = filterOptions.createPredicate(stream);
        assertThat(predicate.test(ENTRY_A)).isFalse();
        assertThat(predicate.test(ENTRY_B)).isTrue();
        assertThat(predicate.test(ENTRY_C)).isFalse();
    }

    private static final class TestJsonStream implements JsonStream<TestEntry> {
        @Override
        public TypeToken<TestEntry> type() {
            return TypeToken.get(TestEntry.class);
        }

        @Override
        public Predicate<TestEntry> device(String device) {
            return entry -> Objects.equals(entry.device, device);
        }

        @Override
        public Predicate<TestEntry> app(String app) {
            return entry -> Objects.equals(entry.app, app);
        }

        @Override
        public Predicate<TestEntry> eventName(String name) {
            return entry -> Objects.equals(entry.name, name);
        }

        @Override
        public String path() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Function<TestEntry, String> formatter() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<String> csvHeader() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Function<TestEntry, List<String>> csvFormatter() {
            throw new UnsupportedOperationException();
        }
    }

    private static final class TestEntry {
        private final String device;
        private final String app;
        private final String name;

        public TestEntry(String device, String app, String name) {
            this.device = device;
            this.app = app;
            this.name = name;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof TestEntry)) {
                return false;
            }
            TestEntry that = (TestEntry) other;
            return Objects.equals(this.device, that.device)
                    && Objects.equals(this.app, that.app)
                    && Objects.equals(this.name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(TestEntry.class, device, app, name);
        }
    }
}
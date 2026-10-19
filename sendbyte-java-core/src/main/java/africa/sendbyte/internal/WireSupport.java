package africa.sendbyte.internal;

import africa.sendbyte.exception.SerializationException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Shared helpers for translating between the public models of a product module and the package
 * private API DTOs that mirror the reviewed OpenAPI contract.
 *
 * <p>Product modules own their own DTOs but not their own copy of this logic. This type is internal
 * SDK plumbing: it is not a stable application API and may change between SDK versions.
 */
public final class WireSupport {
    private WireSupport() {}

    /** Parses an ISO-8601 timestamp, tolerating an absent value. */
    public static OffsetDateTime parseTimestamp(final String value, final String description) {
        try {
            return value == null ? null : OffsetDateTime.parse(value);
        } catch (final DateTimeParseException exception) {
            throw new SerializationException("Could not decode " + description, exception);
        }
    }

    /** Returns an unmodifiable copy, mapping an absent list to the empty list. */
    public static <T> List<T> immutable(final List<T> value) {
        return value == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(value));
    }

    /** Maps each element and returns an unmodifiable list, mapping an absent list to empty. */
    public static <S, T> List<T> mapImmutable(final List<S> value, final Function<S, T> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        if (value == null) {
            return Collections.emptyList();
        }

        final List<T> mapped = new ArrayList<>(value.size());
        for (final S item : value) {
            mapped.add(mapper.apply(item));
        }
        return Collections.unmodifiableList(mapped);
    }

    /** Returns an unmodifiable copy, mapping an absent map to the empty map. */
    public static Map<String, Object> immutableMap(final Map<String, Object> value) {
        return value == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(value));
    }

    /** Requires a non-null, non-blank value. */
    public static String requireText(final String value, final String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " must not be null");
        }
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    /** Requires a non-null, non-blank value between {@code minimum} and {@code maximum} chars. */
    public static String requireText(
            final String value, final String name, final int minimum, final int maximum) {
        Objects.requireNonNull(value, name);
        if (value.trim().isEmpty() || value.length() < minimum || value.length() > maximum) {
            throw new IllegalArgumentException(
                    name
                            + " must be non-blank and contain between "
                            + minimum
                            + " and "
                            + maximum
                            + " characters");
        }
        return value;
    }

    /** Returns null for an absent value and otherwise requires a non-blank value. */
    public static String optionalText(final String value, final String name) {
        if (value == null) {
            return null;
        }
        return WireSupport.requireText(value, name);
    }

    /**
     * Returns null for an absent value and otherwise requires at most {@code maximum} characters.
     */
    public static String optionalText(final String value, final String name, final int maximum) {
        if (value == null) {
            return null;
        }
        if (value.trim().isEmpty() || value.length() > maximum) {
            throw new IllegalArgumentException(
                    name + " must be non-blank and contain at most " + maximum + " characters");
        }
        return value;
    }

    /**
     * Normalizes a wire value for comparison against SDK enum names by upper-casing it and mapping
     * dashes to underscores.
     */
    public static String normalizeWire(final String value) {
        return value == null ? null : value.toUpperCase(Locale.ROOT).replace('-', '_');
    }
}

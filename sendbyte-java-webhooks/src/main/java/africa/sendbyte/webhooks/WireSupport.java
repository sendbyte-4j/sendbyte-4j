package africa.sendbyte.webhooks;

import africa.sendbyte.exception.SerializationException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

final class WireSupport {
    private WireSupport() {}

    static OffsetDateTime parseTimestamp(final String value, final String description) {
        try {
            return value == null ? null : OffsetDateTime.parse(value);
        } catch (final DateTimeParseException exception) {
            throw new SerializationException("Could not decode " + description, exception);
        }
    }

    static <T> List<T> immutable(final List<T> value) {
        return value == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(value));
    }

    static <S, T> List<T> mapImmutable(final List<S> value, final Function<S, T> mapper) {
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

    static String requireText(final String value, final String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " must not be null");
        }
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}

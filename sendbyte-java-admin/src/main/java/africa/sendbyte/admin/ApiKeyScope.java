package africa.sendbyte.admin;

import java.util.Locale;

/** Permission scope for an API key. */
public enum ApiKeyScope {
    SEND_ONLY,
    READ_ONLY,
    FULL_ACCESS,
    UNKNOWN;

    String wireValue() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    static ApiKeyScope fromWire(final String value) {
        if (value == null) {
            return UNKNOWN;
        }
        try {
            return valueOf(value.toUpperCase(Locale.ROOT).replace('-', '_'));
        } catch (final IllegalArgumentException ignored) {
            return UNKNOWN;
        }
    }
}

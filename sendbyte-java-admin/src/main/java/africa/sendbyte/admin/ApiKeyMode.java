package africa.sendbyte.admin;

import java.util.Locale;

/** Runtime mode for an API key. */
public enum ApiKeyMode {
    LIVE,
    TEST,
    UNKNOWN;

    String wireValue() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    static ApiKeyMode fromWire(final String value) {
        if (value == null) {
            return UNKNOWN;
        }
        try {
            return valueOf(value.toUpperCase(Locale.ROOT));
        } catch (final IllegalArgumentException ignored) {
            return UNKNOWN;
        }
    }
}

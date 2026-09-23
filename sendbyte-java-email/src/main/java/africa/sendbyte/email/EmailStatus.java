package africa.sendbyte.email;

import java.util.Locale;

/** Current delivery status returned for an email. */
public enum EmailStatus {
    QUEUED,
    SENT,
    DELIVERED,
    BOUNCED,
    COMPLAINED,
    SUPPRESSED,
    UNKNOWN;

    String wireValue() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    static EmailStatus fromWire(final String value) {
        if (value == null) {
            return UNKNOWN;
        }
        try {
            return valueOf(value.toUpperCase(java.util.Locale.ROOT));
        } catch (final IllegalArgumentException ignored) {
            return UNKNOWN;
        }
    }
}

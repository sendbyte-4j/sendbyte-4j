package africa.sendbyte.email;

/** Current delivery status returned for an email. */
public enum EmailStatus {
    QUEUED,
    SENT,
    DELIVERED,
    BOUNCED,
    COMPLAINED,
    SUPPRESSED,
    UNKNOWN;

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

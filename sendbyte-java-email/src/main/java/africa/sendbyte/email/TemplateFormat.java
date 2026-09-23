package africa.sendbyte.email;

/** Render source format reported for a stored template. */
public enum TemplateFormat {
    MJML,
    HTML,
    TEXT,
    UNKNOWN;

    static TemplateFormat fromWire(final String value) {
        if (value == null) {
            return UNKNOWN;
        }
        try {
            return valueOf(WireSupport.normalizeWire(value));
        } catch (final IllegalArgumentException ignored) {
            return UNKNOWN;
        }
    }
}

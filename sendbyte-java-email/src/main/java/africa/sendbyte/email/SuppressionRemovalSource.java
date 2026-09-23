package africa.sendbyte.email;

/** Source that created a suppression removal request. */
public enum SuppressionRemovalSource {
    API,
    DASHBOARD,
    UNKNOWN;

    static SuppressionRemovalSource fromWire(final String value) {
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

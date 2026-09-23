package africa.sendbyte.marketing;

/** Source that originally placed a subscriber on a marketing list. */
public enum SubscriberSource {
    FORM("form"),
    IMPORT("import"),
    API("api"),
    MANUAL("manual"),
    UNKNOWN(null);

    private final String wireValue;

    SubscriberSource(final String wireValue) {
        this.wireValue = wireValue;
    }

    public String wireValue() {
        return this.wireValue;
    }

    static SubscriberSource fromWire(final String value) {
        final String normalized = WireSupport.normalizeWire(value);
        for (final SubscriberSource source : values()) {
            if (source.name().equals(normalized)) {
                return source;
            }
        }
        return UNKNOWN;
    }
}

package africa.sendbyte.marketing;

/** Opt-in policy for a marketing subscriber list. */
public enum SubscriberListOptIn {
    SINGLE("single"),
    DOUBLE("double"),
    UNKNOWN(null);

    private final String wireValue;

    SubscriberListOptIn(final String wireValue) {
        this.wireValue = wireValue;
    }

    public String wireValue() {
        return this.wireValue;
    }

    static SubscriberListOptIn fromWire(final String value) {
        final String normalized = WireSupport.normalizeWire(value);
        for (final SubscriberListOptIn item : values()) {
            if (item.name().equals(normalized)) {
                return item;
            }
        }
        return UNKNOWN;
    }
}

package africa.sendbyte.marketing;

import africa.sendbyte.internal.WireSupport;

/** Reason an add-subscriber call deliberately left an address unchanged. */
public enum SkippedReason {
    UNSUBSCRIBED("unsubscribed"),
    BRAND_OPT_OUT("brand_opt_out"),
    BOUNCED("bounced"),
    COMPLAINED("complained"),
    UNKNOWN(null);

    private final String wireValue;

    SkippedReason(final String wireValue) {
        this.wireValue = wireValue;
    }

    public String wireValue() {
        return this.wireValue;
    }

    static SkippedReason fromWire(final String value) {
        if (value == null) {
            return null;
        }
        final String normalized = WireSupport.normalizeWire(value);
        for (final SkippedReason reason : values()) {
            if (reason.name().equals(normalized)) {
                return reason;
            }
        }
        return UNKNOWN;
    }
}

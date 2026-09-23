package africa.sendbyte.marketing;

import africa.sendbyte.internal.WireSupport;

/** Subscriber status scoped to a single marketing list. */
public enum SubscriberStatus {
    SUBSCRIBED("subscribed"),
    UNCONFIRMED("unconfirmed"),
    UNSUBSCRIBED("unsubscribed"),
    BOUNCED("bounced"),
    COMPLAINED("complained"),
    UNKNOWN(null);

    private final String wireValue;

    SubscriberStatus(final String wireValue) {
        this.wireValue = wireValue;
    }

    public String wireValue() {
        return this.wireValue;
    }

    static SubscriberStatus fromWire(final String value) {
        final String normalized = WireSupport.normalizeWire(value);
        for (final SubscriberStatus status : values()) {
            if (status.name().equals(normalized)) {
                return status;
            }
        }
        return UNKNOWN;
    }
}

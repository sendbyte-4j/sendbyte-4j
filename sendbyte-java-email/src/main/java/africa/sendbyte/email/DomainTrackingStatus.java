package africa.sendbyte.email;

import africa.sendbyte.internal.WireSupport;

/** Tracking DNS state for a sending domain. */
public enum DomainTrackingStatus {
    OFF,
    PENDING,
    ACTIVE,
    UNKNOWN;

    static DomainTrackingStatus fromWire(final String value) {
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

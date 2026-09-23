package africa.sendbyte.email;

import africa.sendbyte.internal.WireSupport;

/** Verification state returned for a sending domain. */
public enum DomainStatus {
    PENDING,
    VERIFIED,
    UNKNOWN;

    static DomainStatus fromWire(final String value) {
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

package africa.sendbyte.email;

import africa.sendbyte.internal.WireSupport;

/** Status for a suppression removal request. */
public enum SuppressionRemovalStatus {
    PENDING_VERIFICATION,
    REMOVED,
    EXPIRED,
    FAILED,
    CANCELLED,
    UNKNOWN;

    static SuppressionRemovalStatus fromWire(final String value) {
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

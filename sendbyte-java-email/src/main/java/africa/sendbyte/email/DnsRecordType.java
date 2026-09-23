package africa.sendbyte.email;

/** DNS record type required by a domain verification flow. */
public enum DnsRecordType {
    TXT,
    CNAME,
    MX,
    UNKNOWN;

    static DnsRecordType fromWire(final String value) {
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

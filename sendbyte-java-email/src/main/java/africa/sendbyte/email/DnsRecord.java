package africa.sendbyte.email;

/** DNS record published by the API for domain verification or tracking. */
public final class DnsRecord {
    private final DnsRecordType type;
    private final String rawType;
    private final String host;
    private final String name;
    private final String value;
    private final String purpose;
    private final boolean required;

    DnsRecord(
            final String rawType,
            final String host,
            final String name,
            final String value,
            final String purpose,
            final boolean required) {
        this.type = DnsRecordType.fromWire(rawType);
        this.rawType = rawType;
        this.host = host;
        this.name = name;
        this.value = value;
        this.purpose = purpose;
        this.required = required;
    }

    public DnsRecordType type() {
        return this.type;
    }

    public String rawType() {
        return this.rawType;
    }

    public String host() {
        return this.host;
    }

    public String name() {
        return this.name;
    }

    public String value() {
        return this.value;
    }

    public String purpose() {
        return this.purpose;
    }

    public boolean required() {
        return this.required;
    }
}

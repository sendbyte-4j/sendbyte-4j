package africa.sendbyte.email;

import java.time.OffsetDateTime;
import java.util.List;

/** Sending domain and its deliverability configuration. */
public final class Domain {
    private final String id;
    private final String domain;
    private final DomainStatus status;
    private final String rawStatus;
    private final String dkimSelector;
    private final List<DnsRecord> dnsRecords;
    private final OffsetDateTime verifiedAt;
    private final boolean deliverabilityReady;
    private final DomainTracking tracking;
    private final OffsetDateTime createdAt;

    Domain(
            final String id,
            final String domain,
            final String rawStatus,
            final String dkimSelector,
            final List<DnsRecord> dnsRecords,
            final OffsetDateTime verifiedAt,
            final boolean deliverabilityReady,
            final DomainTracking tracking,
            final OffsetDateTime createdAt) {
        this.id = id;
        this.domain = domain;
        this.status = DomainStatus.fromWire(rawStatus);
        this.rawStatus = rawStatus;
        this.dkimSelector = dkimSelector;
        this.dnsRecords = WireSupport.immutable(dnsRecords);
        this.verifiedAt = verifiedAt;
        this.deliverabilityReady = deliverabilityReady;
        this.tracking = tracking;
        this.createdAt = createdAt;
    }

    public String id() {
        return this.id;
    }

    public String domain() {
        return this.domain;
    }

    public DomainStatus status() {
        return this.status;
    }

    public String rawStatus() {
        return this.rawStatus;
    }

    public String dkimSelector() {
        return this.dkimSelector;
    }

    public List<DnsRecord> dnsRecords() {
        return this.dnsRecords;
    }

    public OffsetDateTime verifiedAt() {
        return this.verifiedAt;
    }

    public boolean deliverabilityReady() {
        return this.deliverabilityReady;
    }

    public DomainTracking tracking() {
        return this.tracking;
    }

    public OffsetDateTime createdAt() {
        return this.createdAt;
    }
}

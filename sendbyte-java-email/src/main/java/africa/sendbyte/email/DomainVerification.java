package africa.sendbyte.email;

import java.util.List;

/** Result returned after requesting domain verification. */
public final class DomainVerification {
    private final String id;
    private final String domain;
    private final DomainStatus status;
    private final String rawStatus;
    private final List<DnsRecord> checks;
    private final boolean sesVerified;
    private final boolean deliverabilityReady;
    private final DomainTracking tracking;

    DomainVerification(
            final String id,
            final String domain,
            final String rawStatus,
            final List<DnsRecord> checks,
            final boolean sesVerified,
            final boolean deliverabilityReady,
            final DomainTracking tracking) {
        this.id = id;
        this.domain = domain;
        this.status = DomainStatus.fromWire(rawStatus);
        this.rawStatus = rawStatus;
        this.checks = WireSupport.immutable(checks);
        this.sesVerified = sesVerified;
        this.deliverabilityReady = deliverabilityReady;
        this.tracking = tracking;
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

    public List<DnsRecord> checks() {
        return this.checks;
    }

    public boolean sesVerified() {
        return this.sesVerified;
    }

    public boolean deliverabilityReady() {
        return this.deliverabilityReady;
    }

    public DomainTracking tracking() {
        return this.tracking;
    }
}

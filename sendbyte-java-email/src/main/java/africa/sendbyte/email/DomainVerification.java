package africa.sendbyte.email;

import africa.sendbyte.internal.WireSupport;
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
    private final DomainTrackingStatus tracking;
    private final String rawTracking;

    DomainVerification(
            final String id,
            final String domain,
            final String rawStatus,
            final List<DnsRecord> checks,
            final boolean sesVerified,
            final boolean deliverabilityReady,
            final String rawTracking) {
        this.id = id;
        this.domain = domain;
        this.status = DomainStatus.fromWire(rawStatus);
        this.rawStatus = rawStatus;
        this.checks = WireSupport.immutable(checks);
        this.sesVerified = sesVerified;
        this.deliverabilityReady = deliverabilityReady;
        this.tracking = DomainTrackingStatus.fromWire(rawTracking);
        this.rawTracking = rawTracking;
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

    /**
     * Tracking state reported by the verification response.
     *
     * <p>The verification response reports only the tracking status. The {@code Domain} resource
     * reports the full {@link DomainTracking} configuration.
     */
    public DomainTrackingStatus tracking() {
        return this.tracking;
    }

    /** The tracking value exactly as it appeared on the wire. */
    public String rawTracking() {
        return this.rawTracking;
    }
}

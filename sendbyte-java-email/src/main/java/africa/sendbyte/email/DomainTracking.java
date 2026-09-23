package africa.sendbyte.email;

/** Click/open tracking configuration for a sending domain. */
public final class DomainTracking {
    private final boolean enabled;
    private final String host;
    private final DomainTrackingStatus status;
    private final String rawStatus;

    DomainTracking(final boolean enabled, final String host, final String rawStatus) {
        this.enabled = enabled;
        this.host = host;
        this.status = DomainTrackingStatus.fromWire(rawStatus);
        this.rawStatus = rawStatus;
    }

    public boolean enabled() {
        return this.enabled;
    }

    public String host() {
        return this.host;
    }

    public DomainTrackingStatus status() {
        return this.status;
    }

    public String rawStatus() {
        return this.rawStatus;
    }
}

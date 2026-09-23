package africa.sendbyte.email;

/** Removal request summary nested on a suppression record. */
public final class SuppressionRequestSummary {
    private final String id;
    private final SuppressionRemovalStatus status;
    private final String rawStatus;

    SuppressionRequestSummary(final String id, final String rawStatus) {
        this.id = id;
        this.status = SuppressionRemovalStatus.fromWire(rawStatus);
        this.rawStatus = rawStatus;
    }

    public String id() {
        return this.id;
    }

    public SuppressionRemovalStatus status() {
        return this.status;
    }

    public String rawStatus() {
        return this.rawStatus;
    }
}

package africa.sendbyte.marketing;

/** Result of adding one subscriber to a marketing list. */
public final class AddSubscriberResult {
    private final String id;
    private final String email;
    private final SubscriberStatus status;
    private final String rawStatus;
    private final SkippedReason skippedReason;
    private final String rawSkippedReason;

    AddSubscriberResult(
            final String id,
            final String email,
            final String rawStatus,
            final String rawSkippedReason) {
        this.id = id;
        this.email = email;
        this.status = SubscriberStatus.fromWire(rawStatus);
        this.rawStatus = rawStatus;
        this.skippedReason = SkippedReason.fromWire(rawSkippedReason);
        this.rawSkippedReason = rawSkippedReason;
    }

    public String id() {
        return this.id;
    }

    public String email() {
        return this.email;
    }

    public SubscriberStatus status() {
        return this.status;
    }

    public String rawStatus() {
        return this.rawStatus;
    }

    public SkippedReason skippedReason() {
        return this.skippedReason;
    }

    public String rawSkippedReason() {
        return this.rawSkippedReason;
    }
}

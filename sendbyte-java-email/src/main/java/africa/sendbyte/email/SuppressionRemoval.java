package africa.sendbyte.email;

import java.time.OffsetDateTime;
import java.util.List;

/** Removal request for a suppressed recipient address. */
public final class SuppressionRemoval {
    private final String id;
    private final String address;
    private final String reason;
    private final SuppressionRemovalStatus status;
    private final String rawStatus;
    private final String note;
    private final SuppressionRemovalSource source;
    private final String rawSource;
    private final int verificationSends;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime expiresAt;
    private final OffsetDateTime verifiedAt;
    private final OffsetDateTime removedAt;
    private final OffsetDateTime cancelledAt;
    private final List<SuppressionRemovalTimelineEvent> timeline;

    SuppressionRemoval(
            final String id,
            final String address,
            final String reason,
            final String rawStatus,
            final String note,
            final String rawSource,
            final int verificationSends,
            final OffsetDateTime createdAt,
            final OffsetDateTime expiresAt,
            final OffsetDateTime verifiedAt,
            final OffsetDateTime removedAt,
            final OffsetDateTime cancelledAt,
            final List<SuppressionRemovalTimelineEvent> timeline) {
        this.id = id;
        this.address = address;
        this.reason = reason;
        this.status = SuppressionRemovalStatus.fromWire(rawStatus);
        this.rawStatus = rawStatus;
        this.note = note;
        this.source = SuppressionRemovalSource.fromWire(rawSource);
        this.rawSource = rawSource;
        this.verificationSends = verificationSends;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.verifiedAt = verifiedAt;
        this.removedAt = removedAt;
        this.cancelledAt = cancelledAt;
        this.timeline = WireSupport.immutable(timeline);
    }

    public String id() {
        return this.id;
    }

    public String address() {
        return this.address;
    }

    public String reason() {
        return this.reason;
    }

    public SuppressionRemovalStatus status() {
        return this.status;
    }

    public String rawStatus() {
        return this.rawStatus;
    }

    public String note() {
        return this.note;
    }

    public SuppressionRemovalSource source() {
        return this.source;
    }

    public String rawSource() {
        return this.rawSource;
    }

    public int verificationSends() {
        return this.verificationSends;
    }

    public OffsetDateTime createdAt() {
        return this.createdAt;
    }

    public OffsetDateTime expiresAt() {
        return this.expiresAt;
    }

    public OffsetDateTime verifiedAt() {
        return this.verifiedAt;
    }

    public OffsetDateTime removedAt() {
        return this.removedAt;
    }

    public OffsetDateTime cancelledAt() {
        return this.cancelledAt;
    }

    public List<SuppressionRemovalTimelineEvent> timeline() {
        return this.timeline;
    }
}

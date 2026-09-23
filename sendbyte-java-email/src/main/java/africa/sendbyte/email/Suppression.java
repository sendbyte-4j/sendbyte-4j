package africa.sendbyte.email;

import java.time.OffsetDateTime;

/** Suppressed recipient address and its removal eligibility. */
public final class Suppression {
    private final String address;
    private final String reason;
    private final String source;
    private final OffsetDateTime createdAt;
    private final boolean eligible;
    private final String eligibleReason;
    private final SuppressionRequestSummary request;

    Suppression(
            final String address,
            final String reason,
            final String source,
            final OffsetDateTime createdAt,
            final boolean eligible,
            final String eligibleReason,
            final SuppressionRequestSummary request) {
        this.address = address;
        this.reason = reason;
        this.source = source;
        this.createdAt = createdAt;
        this.eligible = eligible;
        this.eligibleReason = eligibleReason;
        this.request = request;
    }

    public String address() {
        return this.address;
    }

    public String reason() {
        return this.reason;
    }

    public String source() {
        return this.source;
    }

    public OffsetDateTime createdAt() {
        return this.createdAt;
    }

    public boolean eligible() {
        return this.eligible;
    }

    public String eligibleReason() {
        return this.eligibleReason;
    }

    public SuppressionRequestSummary request() {
        return this.request;
    }
}

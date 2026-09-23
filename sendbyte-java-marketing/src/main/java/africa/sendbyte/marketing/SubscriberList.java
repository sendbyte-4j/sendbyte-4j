package africa.sendbyte.marketing;

import java.time.OffsetDateTime;

/** Marketing subscriber list metadata. */
public final class SubscriberList {
    private final String id;
    private final String name;
    private final SubscriberListOptIn optIn;
    private final String rawOptIn;
    private final SubscriberListCounts counts;
    private final OffsetDateTime createdAt;

    SubscriberList(
            final String id,
            final String name,
            final String rawOptIn,
            final SubscriberListCounts counts,
            final OffsetDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.optIn = SubscriberListOptIn.fromWire(rawOptIn);
        this.rawOptIn = rawOptIn;
        this.counts = counts;
        this.createdAt = createdAt;
    }

    public String id() {
        return this.id;
    }

    public String name() {
        return this.name;
    }

    public SubscriberListOptIn optIn() {
        return this.optIn;
    }

    public String rawOptIn() {
        return this.rawOptIn;
    }

    public SubscriberListCounts counts() {
        return this.counts;
    }

    public OffsetDateTime createdAt() {
        return this.createdAt;
    }
}

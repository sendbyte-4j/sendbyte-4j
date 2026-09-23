package africa.sendbyte.marketing;

import africa.sendbyte.internal.WireSupport;
import java.time.OffsetDateTime;
import java.util.Map;

/** Subscriber record scoped to a marketing list. */
public final class Subscriber {
    private final String id;
    private final String email;
    private final String name;
    private final Map<String, Object> attributes;
    private final SubscriberStatus status;
    private final String rawStatus;
    private final SubscriberSource source;
    private final String rawSource;
    private final OffsetDateTime createdAt;

    Subscriber(
            final String id,
            final String email,
            final String name,
            final Map<String, Object> attributes,
            final String rawStatus,
            final String rawSource,
            final OffsetDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.attributes = WireSupport.immutableMap(attributes);
        this.status = SubscriberStatus.fromWire(rawStatus);
        this.rawStatus = rawStatus;
        this.source = SubscriberSource.fromWire(rawSource);
        this.rawSource = rawSource;
        this.createdAt = createdAt;
    }

    public String id() {
        return this.id;
    }

    public String email() {
        return this.email;
    }

    public String name() {
        return this.name;
    }

    public Map<String, Object> attributes() {
        return this.attributes;
    }

    public SubscriberStatus status() {
        return this.status;
    }

    public String rawStatus() {
        return this.rawStatus;
    }

    public SubscriberSource source() {
        return this.source;
    }

    public String rawSource() {
        return this.rawSource;
    }

    public OffsetDateTime createdAt() {
        return this.createdAt;
    }
}

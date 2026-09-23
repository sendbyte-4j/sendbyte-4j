package africa.sendbyte.email;

import africa.sendbyte.internal.WireSupport;
import java.time.OffsetDateTime;
import java.util.Map;

/** Timeline event returned for a suppression removal request. */
public final class SuppressionRemovalTimelineEvent {
    private final String action;
    private final String actor;
    private final OffsetDateTime at;
    private final Map<String, Object> metadata;

    SuppressionRemovalTimelineEvent(
            final String action,
            final String actor,
            final OffsetDateTime at,
            final Map<String, Object> metadata) {
        this.action = action;
        this.actor = actor;
        this.at = at;
        this.metadata = WireSupport.immutableMap(metadata);
    }

    /** What happened, for example {@code created} or {@code verified}. */
    public String action() {
        return this.action;
    }

    /** Who performed the transition, for example {@code api} or {@code system}. */
    public String actor() {
        return this.actor;
    }

    /** When the transition happened. */
    public OffsetDateTime at() {
        return this.at;
    }

    /** Additional event detail, empty when the event carries none. */
    public Map<String, Object> metadata() {
        return this.metadata;
    }
}

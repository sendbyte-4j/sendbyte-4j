package africa.sendbyte.email;

import java.time.OffsetDateTime;

/** Timeline event returned for a suppression removal request. */
public final class SuppressionRemovalTimelineEvent {
    private final String type;
    private final String message;
    private final OffsetDateTime createdAt;

    SuppressionRemovalTimelineEvent(
            final String type, final String message, final OffsetDateTime createdAt) {
        this.type = type;
        this.message = message;
        this.createdAt = createdAt;
    }

    public String type() {
        return this.type;
    }

    public String message() {
        return this.message;
    }

    public OffsetDateTime createdAt() {
        return this.createdAt;
    }
}

package africa.sendbyte.webhooks;

import java.time.OffsetDateTime;

/** Recent delivery attempt for a webhook endpoint. */
public final class WebhookDelivery {
    private final String id;
    private final WebhookEventType eventType;
    private final String rawEventType;
    private final String status;
    private final int attempts;
    private final OffsetDateTime deliveredAt;
    private final OffsetDateTime createdAt;

    WebhookDelivery(
            final String id,
            final String rawEventType,
            final String status,
            final int attempts,
            final OffsetDateTime deliveredAt,
            final OffsetDateTime createdAt) {
        this.id = id;
        this.eventType = WebhookEventType.fromWire(rawEventType);
        this.rawEventType = rawEventType;
        this.status = status;
        this.attempts = attempts;
        this.deliveredAt = deliveredAt;
        this.createdAt = createdAt;
    }

    public String id() {
        return this.id;
    }

    public WebhookEventType eventType() {
        return this.eventType;
    }

    public String rawEventType() {
        return this.rawEventType;
    }

    public String status() {
        return this.status;
    }

    public int attempts() {
        return this.attempts;
    }

    public OffsetDateTime deliveredAt() {
        return this.deliveredAt;
    }

    public OffsetDateTime createdAt() {
        return this.createdAt;
    }
}

package africa.sendbyte.webhooks;

import africa.sendbyte.internal.WireSupport;
import java.time.OffsetDateTime;
import java.util.List;

/** Webhook endpoint metadata returned by the Webhooks API. */
public final class WebhookEndpoint {
    private final String id;
    private final String url;
    private final boolean subscribesToAllEvents;
    private final List<WebhookEventType> events;
    private final List<String> rawEvents;
    private final boolean disabled;
    private final OffsetDateTime disabledAt;
    private final int consecutiveFailures;
    private final OffsetDateTime createdAt;
    private final String secret;

    WebhookEndpoint(
            final String id,
            final String url,
            final boolean subscribesToAllEvents,
            final List<String> rawEvents,
            final boolean disabled,
            final OffsetDateTime disabledAt,
            final int consecutiveFailures,
            final OffsetDateTime createdAt,
            final String secret) {
        this.id = id;
        this.url = url;
        this.subscribesToAllEvents = subscribesToAllEvents;
        this.rawEvents = WireSupport.immutable(rawEvents);
        this.events = WireSupport.mapImmutable(this.rawEvents, WebhookEventType::fromWire);
        this.disabled = disabled;
        this.disabledAt = disabledAt;
        this.consecutiveFailures = consecutiveFailures;
        this.createdAt = createdAt;
        this.secret = secret;
    }

    public String id() {
        return this.id;
    }

    public String url() {
        return this.url;
    }

    public boolean subscribesToAllEvents() {
        return this.subscribesToAllEvents;
    }

    public List<WebhookEventType> events() {
        return this.events;
    }

    public List<String> rawEvents() {
        return this.rawEvents;
    }

    public boolean disabled() {
        return this.disabled;
    }

    public OffsetDateTime disabledAt() {
        return this.disabledAt;
    }

    public int consecutiveFailures() {
        return this.consecutiveFailures;
    }

    public OffsetDateTime createdAt() {
        return this.createdAt;
    }

    public String secret() {
        return this.secret;
    }

    @Override
    public String toString() {
        return "WebhookEndpoint{"
                + "id='"
                + this.id
                + '\''
                + ", url='"
                + this.url
                + '\''
                + ", subscribesToAllEvents="
                + this.subscribesToAllEvents
                + ", disabled="
                + this.disabled
                + ", consecutiveFailures="
                + this.consecutiveFailures
                + ", secret=[REDACTED]"
                + '}';
    }
}

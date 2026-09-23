package africa.sendbyte.webhooks;

import africa.sendbyte.internal.WireSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Request used to create a webhook endpoint. */
public final class CreateWebhookRequest {
    private final String url;
    private final List<WebhookEventType> events;

    private CreateWebhookRequest(final Builder builder) {
        this.url = WireSupport.requireText(builder.url, "webhook endpoint URL");
        this.events = Collections.unmodifiableList(new ArrayList<>(builder.events));
    }

    public static Builder builder() {
        return new Builder();
    }

    public String url() {
        return this.url;
    }

    public List<WebhookEventType> events() {
        return this.events;
    }

    public boolean subscribesToAllEvents() {
        return this.events.isEmpty();
    }

    /** Mutable builder for {@link CreateWebhookRequest}. */
    public static final class Builder {
        private String url;
        private final List<WebhookEventType> events = new ArrayList<>();

        private Builder() {}

        public Builder url(final String url) {
            this.url = url;
            return this;
        }

        public Builder event(final WebhookEventType event) {
            final WebhookEventType value = Objects.requireNonNull(event, "event");
            if (value == WebhookEventType.UNKNOWN) {
                throw new IllegalArgumentException(
                        "webhook event subscription must be a known contract event type");
            }
            this.events.add(value);
            return this;
        }

        public Builder events(final List<WebhookEventType> events) {
            Objects.requireNonNull(events, "events");
            for (final WebhookEventType event : events) {
                this.event(event);
            }
            return this;
        }

        public CreateWebhookRequest build() {
            return new CreateWebhookRequest(this);
        }
    }
}

package africa.sendbyte.webhooks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class WebhookEndpointApiResponse {
    @JsonProperty("id")
    public String id;

    @JsonProperty("url")
    public String url;

    @JsonProperty("events")
    public JsonNode events;

    @JsonProperty("disabled")
    public boolean disabled;

    @JsonProperty("disabled_at")
    public String disabledAt;

    @JsonProperty("consecutive_failures")
    public int consecutiveFailures;

    @JsonProperty("created_at")
    public String createdAt;

    @JsonProperty("secret")
    public String secret;

    WebhookEndpoint toWebhookEndpoint() {
        return new WebhookEndpoint(
                this.id,
                this.url,
                subscribesToAllEvents(),
                eventValues(),
                this.disabled,
                WireSupport.parseTimestamp(this.disabledAt, "webhook disabled timestamp"),
                this.consecutiveFailures,
                WireSupport.parseTimestamp(this.createdAt, "webhook created timestamp"),
                this.secret);
    }

    private boolean subscribesToAllEvents() {
        return this.events == null
                || (this.events.isTextual() && "all".equals(this.events.asText()));
    }

    private List<String> eventValues() {
        if (subscribesToAllEvents() || !this.events.isArray()) {
            return Collections.emptyList();
        }

        final List<String> result = new ArrayList<>();
        for (final JsonNode event : this.events) {
            result.add(event.asText());
        }
        return result;
    }
}

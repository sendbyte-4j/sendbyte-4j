package africa.sendbyte.webhooks;

import com.fasterxml.jackson.annotation.JsonProperty;

final class WebhookDeliveryApiResponse {
    @JsonProperty("id")
    public String id;

    @JsonProperty("event_type")
    public String eventType;

    @JsonProperty("status")
    public String status;

    @JsonProperty("attempts")
    public int attempts;

    @JsonProperty("delivered_at")
    public String deliveredAt;

    @JsonProperty("created_at")
    public String createdAt;

    WebhookDelivery toWebhookDelivery() {
        return new WebhookDelivery(
                this.id,
                this.eventType,
                this.status,
                this.attempts,
                WireSupport.parseTimestamp(this.deliveredAt, "webhook delivered timestamp"),
                WireSupport.parseTimestamp(this.createdAt, "webhook delivery created timestamp"));
    }
}

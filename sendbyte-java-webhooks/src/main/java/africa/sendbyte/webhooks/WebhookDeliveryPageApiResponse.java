package africa.sendbyte.webhooks;

import africa.sendbyte.Page;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

final class WebhookDeliveryPageApiResponse {
    @JsonProperty("data")
    public List<WebhookDeliveryApiResponse> data;

    Page<WebhookDelivery> toPage() {
        return new Page<>(
                WireSupport.mapImmutable(this.data, WebhookDeliveryApiResponse::toWebhookDelivery),
                false);
    }
}

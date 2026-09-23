package africa.sendbyte.webhooks;

import africa.sendbyte.Page;
import africa.sendbyte.internal.WireSupport;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

final class WebhookEndpointPageApiResponse {
    @JsonProperty("data")
    public List<WebhookEndpointApiResponse> data;

    Page<WebhookEndpoint> toPage() {
        return new Page<>(
                WireSupport.mapImmutable(this.data, WebhookEndpointApiResponse::toWebhookEndpoint),
                false);
    }
}

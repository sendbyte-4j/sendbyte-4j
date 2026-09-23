package africa.sendbyte.webhooks;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;

final class WebhookCreateApiRequest {
    @JsonProperty("url")
    public final String url;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("events")
    public final List<String> events;

    private WebhookCreateApiRequest(final String url, final List<String> events) {
        this.url = url;
        this.events = events;
    }

    static WebhookCreateApiRequest from(final CreateWebhookRequest request) {
        final List<String> events =
                request.subscribesToAllEvents()
                        ? null
                        : request.events().stream()
                                .map(WebhookEventType::wireValue)
                                .collect(Collectors.toList());
        return new WebhookCreateApiRequest(request.url(), events);
    }
}

package africa.sendbyte.webhooks;

import com.fasterxml.jackson.annotation.JsonProperty;

final class WebhookReplayApiResponse {
    @JsonProperty("id")
    public String id;

    @JsonProperty("replay_of")
    public String replayOf;

    @JsonProperty("status")
    public String status;

    WebhookReplay toWebhookReplay() {
        return new WebhookReplay(this.id, this.replayOf, this.status);
    }
}

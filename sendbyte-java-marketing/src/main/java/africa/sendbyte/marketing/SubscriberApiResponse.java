package africa.sendbyte.marketing;

import africa.sendbyte.internal.WireSupport;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

final class SubscriberApiResponse {
    @JsonProperty("id")
    public String id;

    @JsonProperty("email")
    public String email;

    @JsonProperty("name")
    public String name;

    @JsonProperty("attributes")
    public Map<String, Object> attributes;

    @JsonProperty("status")
    public String status;

    @JsonProperty("source")
    public String source;

    @JsonProperty("created_at")
    public String createdAt;

    Subscriber toSubscriber() {
        return new Subscriber(
                this.id,
                this.email,
                this.name,
                this.attributes,
                this.status,
                this.source,
                WireSupport.parseTimestamp(this.createdAt, "subscriber created timestamp"));
    }
}

package africa.sendbyte.marketing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

final class SubscriberApiRequest {
    @JsonProperty("email")
    public final String email;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("name")
    public final String name;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("attributes")
    public final Map<String, Object> attributes;

    private SubscriberApiRequest(
            final String email, final String name, final Map<String, Object> attributes) {
        this.email = email;
        this.name = name;
        this.attributes = attributes;
    }

    static SubscriberApiRequest from(final AddSubscriberRequest request) {
        return new SubscriberApiRequest(request.email(), request.name(), request.attributes());
    }

    static SubscriberApiRequest from(final SubscriberImport subscriber) {
        return new SubscriberApiRequest(
                subscriber.email(), subscriber.name(), subscriber.attributes());
    }
}

package africa.sendbyte.marketing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

final class SubscriberListApiRequest {
    @JsonProperty("name")
    public final String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("opt_in")
    public final String optIn;

    private SubscriberListApiRequest(final String name, final String optIn) {
        this.name = name;
        this.optIn = optIn;
    }

    static SubscriberListApiRequest from(final CreateSubscriberListRequest request) {
        return new SubscriberListApiRequest(
                request.name(), request.optIn() == null ? null : request.optIn().wireValue());
    }
}

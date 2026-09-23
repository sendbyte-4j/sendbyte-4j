package africa.sendbyte.marketing;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;

final class ImportSubscribersApiRequest {
    @JsonProperty("subscribers")
    public final List<SubscriberApiRequest> subscribers;

    private ImportSubscribersApiRequest(final List<SubscriberApiRequest> subscribers) {
        this.subscribers = subscribers;
    }

    static ImportSubscribersApiRequest from(final ImportSubscribersRequest request) {
        return new ImportSubscribersApiRequest(
                request.subscribers().stream()
                        .map(SubscriberApiRequest::from)
                        .collect(Collectors.toList()));
    }
}

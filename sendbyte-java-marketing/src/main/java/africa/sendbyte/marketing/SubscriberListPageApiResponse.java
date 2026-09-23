package africa.sendbyte.marketing;

import africa.sendbyte.Page;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

final class SubscriberListPageApiResponse {
    @JsonProperty("data")
    public List<SubscriberListApiResponse> data;

    Page<SubscriberList> toPage() {
        return new Page<>(
                WireSupport.mapImmutable(this.data, SubscriberListApiResponse::toSubscriberList),
                false);
    }
}

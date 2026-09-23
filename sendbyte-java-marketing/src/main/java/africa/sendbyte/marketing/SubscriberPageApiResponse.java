package africa.sendbyte.marketing;

import africa.sendbyte.internal.WireSupport;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

final class SubscriberPageApiResponse {
    @JsonProperty("data")
    public List<SubscriberApiResponse> data;

    @JsonProperty("total")
    public int total;

    @JsonProperty("cap")
    public Integer cap;

    @JsonProperty("used")
    public int used;

    SubscriberPage toSubscriberPage() {
        return new SubscriberPage(
                WireSupport.mapImmutable(this.data, SubscriberApiResponse::toSubscriber),
                this.total,
                this.cap,
                this.used);
    }
}

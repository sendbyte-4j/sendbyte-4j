package africa.sendbyte.email;

import africa.sendbyte.Page;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

final class SuppressionPageApiResponse {
    @JsonProperty("data")
    public List<SuppressionApiResponse> data;

    Page<Suppression> toPage() {
        return new Page<>(
                WireSupport.mapImmutable(this.data, SuppressionApiResponse::toSuppression), false);
    }
}

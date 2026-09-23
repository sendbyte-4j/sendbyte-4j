package africa.sendbyte.email;

import africa.sendbyte.Page;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

final class SuppressionRemovalPageApiResponse {
    @JsonProperty("data")
    public List<SuppressionRemovalApiResponse> data;

    Page<SuppressionRemoval> toPage() {
        return new Page<>(
                WireSupport.mapImmutable(
                        this.data, SuppressionRemovalApiResponse::toSuppressionRemoval),
                false);
    }
}

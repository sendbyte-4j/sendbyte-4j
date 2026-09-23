package africa.sendbyte.email;

import africa.sendbyte.Page;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

final class DomainPageApiResponse {
    @JsonProperty("data")
    public List<DomainApiResponse> data;

    Page<Domain> toPage() {
        return new Page<>(WireSupport.mapImmutable(this.data, DomainApiResponse::toDomain), false);
    }
}

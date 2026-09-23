package africa.sendbyte.email;

import africa.sendbyte.Page;
import africa.sendbyte.internal.WireSupport;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

final class TemplatePageApiResponse {
    @JsonProperty("data")
    public List<TemplateApiResponse> data;

    Page<Template> toPage() {
        return new Page<>(
                WireSupport.mapImmutable(this.data, TemplateApiResponse::toTemplate), false);
    }
}

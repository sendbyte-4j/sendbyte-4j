package africa.sendbyte.admin;

import africa.sendbyte.Page;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class ApiKeyPageApiResponse {
    @JsonProperty("data")
    public List<ApiKeyApiResponse> data;

    Page<ApiKey> toPage() {
        if (this.data == null) {
            return new Page<>(Collections.emptyList(), false);
        }

        final List<ApiKey> keys = new ArrayList<>(this.data.size());
        for (final ApiKeyApiResponse key : this.data) {
            keys.add(key.toApiKey());
        }
        return new Page<>(keys, false);
    }
}

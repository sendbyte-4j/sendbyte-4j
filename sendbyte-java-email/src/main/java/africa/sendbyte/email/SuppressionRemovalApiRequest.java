package africa.sendbyte.email;

import africa.sendbyte.email.request.CreateSuppressionRemovalRequest;
import com.fasterxml.jackson.annotation.JsonProperty;

final class SuppressionRemovalApiRequest {
    @JsonProperty("address")
    private final String address;

    @JsonProperty("note")
    private final String note;

    private SuppressionRemovalApiRequest(final CreateSuppressionRemovalRequest request) {
        this.address = request.address();
        this.note = request.note();
    }

    static SuppressionRemovalApiRequest from(final CreateSuppressionRemovalRequest request) {
        return new SuppressionRemovalApiRequest(request);
    }
}

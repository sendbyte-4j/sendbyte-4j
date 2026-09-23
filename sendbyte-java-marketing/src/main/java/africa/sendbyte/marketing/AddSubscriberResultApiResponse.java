package africa.sendbyte.marketing;

import com.fasterxml.jackson.annotation.JsonProperty;

final class AddSubscriberResultApiResponse {
    @JsonProperty("id")
    public String id;

    @JsonProperty("email")
    public String email;

    @JsonProperty("status")
    public String status;

    @JsonProperty("skipped_reason")
    public String skippedReason;

    AddSubscriberResult toAddSubscriberResult() {
        return new AddSubscriberResult(this.id, this.email, this.status, this.skippedReason);
    }
}

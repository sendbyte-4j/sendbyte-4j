package africa.sendbyte.email;

import com.fasterxml.jackson.annotation.JsonProperty;

final class SuppressionApiResponse {
    @JsonProperty("address")
    public String address;

    @JsonProperty("reason")
    public String reason;

    @JsonProperty("source")
    public String source;

    @JsonProperty("created_at")
    public String createdAt;

    @JsonProperty("eligible")
    public boolean eligible;

    @JsonProperty("eligible_reason")
    public String eligibleReason;

    @JsonProperty("request")
    public RemovalSummaryApiResponse request;

    Suppression toSuppression() {
        return new Suppression(
                this.address,
                this.reason,
                this.source,
                WireSupport.parseTimestamp(this.createdAt, "suppression created timestamp"),
                this.eligible,
                this.eligibleReason,
                this.request == null ? null : request.toSuppressionRequestSummary());
    }

    static final class RemovalSummaryApiResponse {
        @JsonProperty("id")
        public String id;

        @JsonProperty("status")
        public String status;

        SuppressionRequestSummary toSuppressionRequestSummary() {
            return new SuppressionRequestSummary(this.id, this.status);
        }
    }
}

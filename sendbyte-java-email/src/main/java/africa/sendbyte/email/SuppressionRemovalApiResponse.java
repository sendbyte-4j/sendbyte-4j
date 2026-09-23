package africa.sendbyte.email;

import africa.sendbyte.internal.WireSupport;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

final class SuppressionRemovalApiResponse {
    @JsonProperty("id")
    public String id;

    @JsonProperty("address")
    public String address;

    @JsonProperty("reason")
    public String reason;

    @JsonProperty("status")
    public String status;

    @JsonProperty("note")
    public String note;

    @JsonProperty("source")
    public String source;

    @JsonProperty("verification_sends")
    public int verificationSends;

    @JsonProperty("created_at")
    public String createdAt;

    @JsonProperty("expires_at")
    public String expiresAt;

    @JsonProperty("verified_at")
    public String verifiedAt;

    @JsonProperty("removed_at")
    public String removedAt;

    @JsonProperty("cancelled_at")
    public String cancelledAt;

    @JsonProperty("timeline")
    public List<TimelineEventApiResponse> timeline;

    SuppressionRemoval toSuppressionRemoval() {
        return new SuppressionRemoval(
                this.id,
                this.address,
                this.reason,
                this.status,
                this.note,
                this.source,
                this.verificationSends,
                WireSupport.parseTimestamp(this.createdAt, "suppression removal created timestamp"),
                WireSupport.parseTimestamp(this.expiresAt, "suppression removal expiry timestamp"),
                WireSupport.parseTimestamp(
                        this.verifiedAt, "suppression removal verified timestamp"),
                WireSupport.parseTimestamp(this.removedAt, "suppression removal removed timestamp"),
                WireSupport.parseTimestamp(
                        this.cancelledAt, "suppression removal cancelled timestamp"),
                WireSupport.mapImmutable(
                        this.timeline,
                        TimelineEventApiResponse::toSuppressionRemovalTimelineEvent));
    }

    static final class TimelineEventApiResponse {
        @JsonProperty("type")
        public String type;

        @JsonProperty("message")
        public String message;

        @JsonProperty("created_at")
        public String createdAt;

        SuppressionRemovalTimelineEvent toSuppressionRemovalTimelineEvent() {
            return new SuppressionRemovalTimelineEvent(
                    this.type,
                    this.message,
                    WireSupport.parseTimestamp(
                            this.createdAt, "suppression removal timeline timestamp"));
        }
    }
}

package africa.sendbyte.marketing;

import africa.sendbyte.internal.WireSupport;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

final class ImportSummaryApiResponse {
    @JsonProperty("imported")
    public int imported;

    @JsonProperty("updated")
    public int updated;

    @JsonProperty("invalid")
    public int invalid;

    @JsonProperty("skipped")
    public int skipped;

    @JsonProperty("overCap")
    public int overCap;

    @JsonProperty("cap")
    public Integer cap;

    @JsonProperty("rejected")
    public List<RejectedSubscriberApiResponse> rejected;

    ImportSummary toImportSummary() {
        return new ImportSummary(
                this.imported,
                this.updated,
                this.invalid,
                this.skipped,
                this.overCap,
                this.cap,
                WireSupport.mapImmutable(
                        this.rejected, RejectedSubscriberApiResponse::toRejectedSubscriber));
    }

    static final class RejectedSubscriberApiResponse {
        @JsonProperty("email")
        public String email;

        @JsonProperty("reason")
        public String reason;

        RejectedSubscriber toRejectedSubscriber() {
            return new RejectedSubscriber(this.email, this.reason);
        }
    }
}

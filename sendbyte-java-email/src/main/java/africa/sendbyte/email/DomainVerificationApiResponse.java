package africa.sendbyte.email;

import africa.sendbyte.internal.WireSupport;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Wire DTO for the domain verification response.
 *
 * <p>The verify operation answers with a different shape than the {@code Domain} resource: it
 * carries {@code checks} instead of {@code dns_records}, omits {@code dkim_selector}, {@code
 * verified_at} and {@code created_at}, and reports {@code tracking} as a bare status string rather
 * than the tracking object used by {@code Domain}.
 */
final class DomainVerificationApiResponse {
    @JsonProperty("id")
    public String id;

    @JsonProperty("domain")
    public String domain;

    @JsonProperty("status")
    public String status;

    @JsonProperty("checks")
    public List<DomainApiResponse.DnsRecordApiResponse> checks;

    @JsonProperty("ses_verified")
    public boolean sesVerified;

    @JsonProperty("deliverability_ready")
    public boolean deliverabilityReady;

    @JsonProperty("tracking")
    public String tracking;

    DomainVerification toVerification() {
        return new DomainVerification(
                this.id,
                this.domain,
                this.status,
                WireSupport.mapImmutable(
                        this.checks, DomainApiResponse.DnsRecordApiResponse::toDnsRecord),
                this.sesVerified,
                this.deliverabilityReady,
                this.tracking);
    }
}

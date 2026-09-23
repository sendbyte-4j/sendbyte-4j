package africa.sendbyte.email;

import africa.sendbyte.internal.WireSupport;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

final class DomainApiResponse {
    @JsonProperty("id")
    public String id;

    @JsonProperty("domain")
    public String domain;

    @JsonProperty("status")
    public String status;

    @JsonProperty("dkim_selector")
    public String dkimSelector;

    @JsonProperty("dns_records")
    public List<DnsRecordApiResponse> dnsRecords;

    @JsonProperty("checks")
    public List<DnsRecordApiResponse> checks;

    @JsonProperty("verified_at")
    public String verifiedAt;

    @JsonProperty("deliverability_ready")
    public boolean deliverabilityReady;

    @JsonProperty("ses_verified")
    public boolean sesVerified;

    @JsonProperty("tracking")
    public DomainTrackingApiResponse tracking;

    @JsonProperty("created_at")
    public String createdAt;

    Domain toDomain() {
        return new Domain(
                this.id,
                this.domain,
                this.status,
                this.dkimSelector,
                WireSupport.mapImmutable(this.dnsRecords, DnsRecordApiResponse::toDnsRecord),
                WireSupport.parseTimestamp(this.verifiedAt, "domain verified timestamp"),
                this.deliverabilityReady,
                this.tracking == null ? null : tracking.toDomainTracking(),
                WireSupport.parseTimestamp(this.createdAt, "domain created timestamp"));
    }

    DomainVerification toVerification() {
        return new DomainVerification(
                this.id,
                this.domain,
                this.status,
                WireSupport.mapImmutable(this.checks, DnsRecordApiResponse::toDnsRecord),
                this.sesVerified,
                this.deliverabilityReady,
                this.tracking == null ? null : tracking.toDomainTracking());
    }

    static final class DnsRecordApiResponse {
        @JsonProperty("type")
        public String type;

        @JsonProperty("host")
        public String host;

        @JsonProperty("name")
        public String name;

        @JsonProperty("value")
        public String value;

        @JsonProperty("purpose")
        public String purpose;

        @JsonProperty("required")
        public boolean required;

        DnsRecord toDnsRecord() {
            return new DnsRecord(
                    this.type, this.host, this.name, this.value, this.purpose, this.required);
        }
    }

    static final class DomainTrackingApiResponse {
        @JsonProperty("enabled")
        public boolean enabled;

        @JsonProperty("host")
        public String host;

        @JsonProperty("status")
        public String status;

        DomainTracking toDomainTracking() {
            return new DomainTracking(this.enabled, this.host, this.status);
        }
    }
}

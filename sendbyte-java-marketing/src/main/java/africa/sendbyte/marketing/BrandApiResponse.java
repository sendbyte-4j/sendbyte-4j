package africa.sendbyte.marketing;

import com.fasterxml.jackson.annotation.JsonProperty;

final class BrandApiResponse {
    @JsonProperty("id")
    public String id;

    @JsonProperty("name")
    public String name;

    @JsonProperty("public_name")
    public String publicName;

    @JsonProperty("logo_url")
    public String logoUrl;

    @JsonProperty("from_address")
    public String fromAddress;

    @JsonProperty("postal_address")
    public String postalAddress;

    @JsonProperty("timezone")
    public String timezone;

    @JsonProperty("created_at")
    public String createdAt;

    Brand toBrand() {
        return new Brand(
                this.id,
                this.name,
                this.publicName,
                this.logoUrl,
                this.fromAddress,
                this.postalAddress,
                this.timezone,
                WireSupport.parseTimestamp(this.createdAt, "brand created timestamp"));
    }
}

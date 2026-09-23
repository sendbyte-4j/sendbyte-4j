package africa.sendbyte.marketing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

final class BrandApiRequest {
    @JsonProperty("name")
    public final String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("public_name")
    public final String publicName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("from_address")
    public final String fromAddress;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("postal_address")
    public final String postalAddress;

    private BrandApiRequest(
            final String name,
            final String publicName,
            final String fromAddress,
            final String postalAddress) {
        this.name = name;
        this.publicName = publicName;
        this.fromAddress = fromAddress;
        this.postalAddress = postalAddress;
    }

    static BrandApiRequest from(final CreateBrandRequest request) {
        return new BrandApiRequest(
                request.name(),
                request.publicName(),
                request.fromAddress(),
                request.postalAddress());
    }
}

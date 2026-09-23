package africa.sendbyte.marketing;

import africa.sendbyte.internal.WireSupport;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

final class BrandPageApiResponse {
    @JsonProperty("data")
    public List<BrandApiResponse> data;

    @JsonProperty("cap")
    public Integer cap;

    @JsonProperty("used")
    public int used;

    BrandPage toBrandPage() {
        return new BrandPage(
                WireSupport.mapImmutable(this.data, BrandApiResponse::toBrand),
                this.cap,
                this.used);
    }
}

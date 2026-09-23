package africa.sendbyte.email;

import com.fasterxml.jackson.annotation.JsonProperty;

final class DomainTrackingApiRequest {
    @JsonProperty("enabled")
    private final boolean enabled;

    DomainTrackingApiRequest(final boolean enabled) {
        this.enabled = enabled;
    }
}

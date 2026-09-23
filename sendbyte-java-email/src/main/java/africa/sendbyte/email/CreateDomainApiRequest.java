package africa.sendbyte.email;

import com.fasterxml.jackson.annotation.JsonProperty;

final class CreateDomainApiRequest {
    @JsonProperty("domain")
    private final String domain;

    private CreateDomainApiRequest(final CreateDomainRequest request) {
        this.domain = request.domain();
    }

    static CreateDomainApiRequest from(final CreateDomainRequest request) {
        return new CreateDomainApiRequest(request);
    }
}

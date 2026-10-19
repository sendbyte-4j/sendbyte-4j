package africa.sendbyte.email.request;

import africa.sendbyte.internal.WireSupport;

/** Request body for creating a sending domain. */
public final class CreateDomainRequest {
    private final String domain;

    private CreateDomainRequest(final Builder builder) {
        this.domain = WireSupport.requireText(builder.domain, "domain", 4, 253);
    }

    public static Builder builder() {
        return new Builder();
    }

    public String domain() {
        return this.domain;
    }

    public static final class Builder {
        private String domain;

        private Builder() {}

        public Builder domain(final String domain) {
            this.domain = domain;
            return this;
        }

        public CreateDomainRequest build() {
            return new CreateDomainRequest(this);
        }
    }
}

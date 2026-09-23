package africa.sendbyte.marketing;

/** Request used to create a marketing brand. */
public final class CreateBrandRequest {
    private final String name;
    private final String publicName;
    private final String fromAddress;
    private final String postalAddress;

    private CreateBrandRequest(final Builder builder) {
        this.name = WireSupport.requireText(builder.name, "brand name");
        this.publicName = WireSupport.optionalText(builder.publicName, "brand public name");
        this.fromAddress = WireSupport.optionalText(builder.fromAddress, "brand from address");
        this.postalAddress =
                WireSupport.optionalText(builder.postalAddress, "brand postal address");
    }

    public static Builder builder() {
        return new Builder();
    }

    public String name() {
        return this.name;
    }

    public String publicName() {
        return this.publicName;
    }

    public String fromAddress() {
        return this.fromAddress;
    }

    public String postalAddress() {
        return this.postalAddress;
    }

    /** Mutable builder for {@link CreateBrandRequest}. */
    public static final class Builder {
        private String name;
        private String publicName;
        private String fromAddress;
        private String postalAddress;

        private Builder() {}

        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        public Builder publicName(final String publicName) {
            this.publicName = publicName;
            return this;
        }

        public Builder fromAddress(final String fromAddress) {
            this.fromAddress = fromAddress;
            return this;
        }

        public Builder postalAddress(final String postalAddress) {
            this.postalAddress = postalAddress;
            return this;
        }

        public CreateBrandRequest build() {
            return new CreateBrandRequest(this);
        }
    }
}

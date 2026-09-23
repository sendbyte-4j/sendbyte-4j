package africa.sendbyte.email;

import africa.sendbyte.internal.WireSupport;

/** Request body for asking SendByte to remove an address from suppression. */
public final class CreateSuppressionRemovalRequest {
    private final String address;
    private final String note;

    private CreateSuppressionRemovalRequest(final Builder builder) {
        this.address = WireSupport.requireText(builder.address, "address", 3, 320);
        this.note = WireSupport.optionalText(builder.note, "note", 1_000);
    }

    public static Builder builder() {
        return new Builder();
    }

    public String address() {
        return this.address;
    }

    public String note() {
        return this.note;
    }

    public static final class Builder {
        private String address;
        private String note;

        private Builder() {}

        public Builder address(final String address) {
            this.address = address;
            return this;
        }

        public Builder note(final String note) {
            this.note = note;
            return this;
        }

        public CreateSuppressionRemovalRequest build() {
            return new CreateSuppressionRemovalRequest(this);
        }
    }
}

package africa.sendbyte.marketing;

import africa.sendbyte.internal.WireSupport;

/** Request used to create a subscriber list under a brand. */
public final class CreateSubscriberListRequest {
    private final String name;
    private final SubscriberListOptIn optIn;

    private CreateSubscriberListRequest(final Builder builder) {
        this.name = WireSupport.requireText(builder.name, "subscriber list name");
        if (builder.optIn == SubscriberListOptIn.UNKNOWN) {
            throw new IllegalArgumentException("subscriber list opt-in policy must be known");
        }
        this.optIn = builder.optIn;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String name() {
        return this.name;
    }

    public SubscriberListOptIn optIn() {
        return this.optIn;
    }

    /** Mutable builder for {@link CreateSubscriberListRequest}. */
    public static final class Builder {
        private String name;
        private SubscriberListOptIn optIn;

        private Builder() {}

        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        public Builder optIn(final SubscriberListOptIn optIn) {
            this.optIn = optIn;
            return this;
        }

        public CreateSubscriberListRequest build() {
            return new CreateSubscriberListRequest(this);
        }
    }
}

package africa.sendbyte.marketing;

import africa.sendbyte.internal.WireSupport;
import java.util.Map;

/** One subscriber row in an import request. */
public final class SubscriberImport {
    private final String email;
    private final String name;
    private final Map<String, Object> attributes;

    private SubscriberImport(final Builder builder) {
        this.email = WireSupport.requireText(builder.email, "import subscriber email");
        this.name = WireSupport.optionalText(builder.name, "import subscriber name");
        this.attributes = WireSupport.immutableMap(builder.attributes);
    }

    public static Builder builder() {
        return new Builder();
    }

    public String email() {
        return this.email;
    }

    public String name() {
        return this.name;
    }

    public Map<String, Object> attributes() {
        return this.attributes;
    }

    /** Mutable builder for {@link SubscriberImport}. */
    public static final class Builder {
        private String email;
        private String name;
        private Map<String, Object> attributes;

        private Builder() {}

        public Builder email(final String email) {
            this.email = email;
            return this;
        }

        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        public Builder attributes(final Map<String, Object> attributes) {
            this.attributes = attributes;
            return this;
        }

        public SubscriberImport build() {
            return new SubscriberImport(this);
        }
    }
}

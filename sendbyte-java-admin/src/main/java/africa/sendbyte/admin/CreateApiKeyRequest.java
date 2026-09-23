package africa.sendbyte.admin;

import java.util.Objects;

/** Request body for creating an API key. */
public final class CreateApiKeyRequest {
    private final String name;
    private final ApiKeyScope scope;
    private final ApiKeyMode mode;

    private CreateApiKeyRequest(final Builder builder) {
        this.name = requireName(builder.name);
        this.scope = requireKnown(builder.scope, "scope");
        this.mode = requireKnown(builder.mode, "mode");
    }

    public static Builder builder() {
        return new Builder();
    }

    public String name() {
        return this.name;
    }

    public ApiKeyScope scope() {
        return this.scope;
    }

    public ApiKeyMode mode() {
        return this.mode;
    }

    static String requireName(final String value) {
        Objects.requireNonNull(value, "name");
        if (value.trim().isEmpty() || value.length() > 100) {
            throw new IllegalArgumentException(
                    "name must be non-blank and contain at most 100 characters");
        }
        return value;
    }

    private static <T extends Enum<T>> T requireKnown(final T value, final String name) {
        Objects.requireNonNull(value, name);
        if ("UNKNOWN".equals(value.name())) {
            throw new IllegalArgumentException(name + " must be a known API value");
        }
        return value;
    }

    public static final class Builder {
        private String name;
        private ApiKeyScope scope;
        private ApiKeyMode mode;

        private Builder() {}

        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        public Builder scope(final ApiKeyScope scope) {
            this.scope = scope;
            return this;
        }

        public Builder mode(final ApiKeyMode mode) {
            this.mode = mode;
            return this;
        }

        public CreateApiKeyRequest build() {
            return new CreateApiKeyRequest(this);
        }
    }
}

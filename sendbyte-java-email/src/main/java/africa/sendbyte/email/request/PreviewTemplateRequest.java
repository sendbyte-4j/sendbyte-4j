package africa.sendbyte.email.request;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Variables used when previewing a saved template. */
public final class PreviewTemplateRequest {
    private final Map<String, Object> variables;

    private PreviewTemplateRequest(final Builder builder) {
        this.variables =
                builder.variables.isEmpty()
                        ? null
                        : Collections.unmodifiableMap(new LinkedHashMap<>(builder.variables));
    }

    public static Builder builder() {
        return new Builder();
    }

    public Map<String, Object> variables() {
        return this.variables;
    }

    public static final class Builder {
        private final Map<String, Object> variables = new LinkedHashMap<>();

        private Builder() {}

        public Builder variable(final String name, final Object value) {
            this.variables.put(Objects.requireNonNull(name, "name"), value);
            return this;
        }

        public PreviewTemplateRequest build() {
            return new PreviewTemplateRequest(this);
        }
    }
}

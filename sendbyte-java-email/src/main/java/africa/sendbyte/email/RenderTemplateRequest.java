package africa.sendbyte.email;

import africa.sendbyte.internal.WireSupport;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Request body for rendering inline template content. */
public final class RenderTemplateRequest {
    private final String subject;
    private final String html;
    private final String text;
    private final String mjml;
    private final Map<String, Object> variables;

    private RenderTemplateRequest(final Builder builder) {
        this.subject = WireSupport.requireText(builder.subject, "subject", 1, 998);
        this.html = WireSupport.optionalText(builder.html, "html", 500_000);
        this.text = WireSupport.optionalText(builder.text, "text", 500_000);
        this.mjml = WireSupport.optionalText(builder.mjml, "mjml", 500_000);
        if (this.html == null && this.text == null && this.mjml == null) {
            throw new IllegalArgumentException("At least one of html, text, or mjml is required");
        }
        this.variables =
                builder.variables.isEmpty()
                        ? null
                        : Collections.unmodifiableMap(new LinkedHashMap<>(builder.variables));
    }

    public static Builder builder() {
        return new Builder();
    }

    public String subject() {
        return this.subject;
    }

    public String html() {
        return this.html;
    }

    public String text() {
        return this.text;
    }

    public String mjml() {
        return this.mjml;
    }

    public Map<String, Object> variables() {
        return this.variables;
    }

    public static final class Builder {
        private String subject;
        private String html;
        private String text;
        private String mjml;
        private final Map<String, Object> variables = new LinkedHashMap<>();

        private Builder() {}

        public Builder subject(final String subject) {
            this.subject = subject;
            return this;
        }

        public Builder html(final String html) {
            this.html = html;
            return this;
        }

        public Builder text(final String text) {
            this.text = text;
            return this;
        }

        public Builder mjml(final String mjml) {
            this.mjml = mjml;
            return this;
        }

        public Builder variable(final String name, final Object value) {
            this.variables.put(Objects.requireNonNull(name, "name"), value);
            return this;
        }

        public RenderTemplateRequest build() {
            return new RenderTemplateRequest(this);
        }
    }
}

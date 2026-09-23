package africa.sendbyte.email;

import africa.sendbyte.internal.WireSupport;

/** Request body used when creating or replacing a saved template. */
public final class TemplateInput {
    private final String name;
    private final String subject;
    private final String html;
    private final String text;
    private final String mjml;

    private TemplateInput(final Builder builder) {
        this.name = validateName(builder.name);
        this.subject = WireSupport.requireText(builder.subject, "subject", 1, 998);
        this.html = WireSupport.optionalText(builder.html, "html", 500_000);
        this.text = WireSupport.optionalText(builder.text, "text", 500_000);
        this.mjml = WireSupport.optionalText(builder.mjml, "mjml", 500_000);

        if (this.html == null && this.text == null && this.mjml == null) {
            throw new IllegalArgumentException("At least one of html, text, or mjml is required");
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public String name() {
        return this.name;
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

    private static String validateName(final String value) {
        final String name = WireSupport.requireText(value, "name", 1, 100);
        for (int index = 0; index < name.length(); index++) {
            final char current = name.charAt(index);
            if ((current >= 'a' && current <= 'z')
                    || (current >= 'A' && current <= 'Z')
                    || (current >= '0' && current <= '9')
                    || current == '-'
                    || current == '_') {
                continue;
            }
            throw new IllegalArgumentException(
                    "name may contain only letters, numbers, hyphens, and underscores");
        }
        return name;
    }

    public static final class Builder {
        private String name;
        private String subject;
        private String html;
        private String text;
        private String mjml;

        private Builder() {}

        public Builder name(final String name) {
            this.name = name;
            return this;
        }

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

        public TemplateInput build() {
            return new TemplateInput(this);
        }
    }
}

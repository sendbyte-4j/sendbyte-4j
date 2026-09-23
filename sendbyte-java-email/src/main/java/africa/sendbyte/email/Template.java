package africa.sendbyte.email;

import java.time.OffsetDateTime;

/** Saved email template returned by the API. */
public final class Template {
    private final String id;
    private final String name;
    private final String subject;
    private final int version;
    private final TemplateFormat format;
    private final String rawFormat;
    private final String html;
    private final String text;
    private final String mjml;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    Template(
            final String id,
            final String name,
            final String subject,
            final int version,
            final String rawFormat,
            final String html,
            final String text,
            final String mjml,
            final OffsetDateTime createdAt,
            final OffsetDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.subject = subject;
        this.version = version;
        this.format = TemplateFormat.fromWire(rawFormat);
        this.rawFormat = rawFormat;
        this.html = html;
        this.text = text;
        this.mjml = mjml;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String id() {
        return this.id;
    }

    public String name() {
        return this.name;
    }

    public String subject() {
        return this.subject;
    }

    public int version() {
        return this.version;
    }

    public TemplateFormat format() {
        return this.format;
    }

    public String rawFormat() {
        return this.rawFormat;
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

    public OffsetDateTime createdAt() {
        return this.createdAt;
    }

    public OffsetDateTime updatedAt() {
        return this.updatedAt;
    }
}

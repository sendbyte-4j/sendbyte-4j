package africa.sendbyte.email;

import africa.sendbyte.internal.WireSupport;
import com.fasterxml.jackson.annotation.JsonProperty;

final class TemplateApiResponse {
    @JsonProperty("id")
    public String id;

    @JsonProperty("name")
    public String name;

    @JsonProperty("subject")
    public String subject;

    @JsonProperty("version")
    public int version;

    @JsonProperty("format")
    public String format;

    @JsonProperty("html")
    public String html;

    @JsonProperty("text")
    public String text;

    @JsonProperty("mjml")
    public String mjml;

    @JsonProperty("created_at")
    public String createdAt;

    @JsonProperty("updated_at")
    public String updatedAt;

    Template toTemplate() {
        return new Template(
                this.id,
                this.name,
                this.subject,
                this.version,
                this.format,
                this.html,
                this.text,
                this.mjml,
                WireSupport.parseTimestamp(this.createdAt, "template created timestamp"),
                WireSupport.parseTimestamp(this.updatedAt, "template updated timestamp"));
    }
}

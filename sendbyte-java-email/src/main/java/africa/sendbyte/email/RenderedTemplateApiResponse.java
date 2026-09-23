package africa.sendbyte.email;

import com.fasterxml.jackson.annotation.JsonProperty;

final class RenderedTemplateApiResponse {
    @JsonProperty("subject")
    public String subject;

    @JsonProperty("html")
    public String html;

    @JsonProperty("text")
    public String text;

    RenderedTemplate toRenderedTemplate() {
        return new RenderedTemplate(this.subject, this.html, this.text);
    }
}

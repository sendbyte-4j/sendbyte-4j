package africa.sendbyte.email;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

final class TemplateApiRequest {
    @JsonProperty("name")
    private final String name;

    @JsonProperty("subject")
    private final String subject;

    @JsonProperty("html")
    private final String html;

    @JsonProperty("text")
    private final String text;

    @JsonProperty("mjml")
    private final String mjml;

    @JsonProperty("variables")
    private final Map<String, Object> variables;

    private TemplateApiRequest(final TemplateInput input) {
        this.name = input.name();
        this.subject = input.subject();
        this.html = input.html();
        this.text = input.text();
        this.mjml = input.mjml();
        this.variables = null;
    }

    private TemplateApiRequest(final RenderTemplateRequest request) {
        this.name = null;
        this.subject = request.subject();
        this.html = request.html();
        this.text = request.text();
        this.mjml = request.mjml();
        this.variables = request.variables();
    }

    static TemplateApiRequest from(final TemplateInput input) {
        return new TemplateApiRequest(input);
    }

    static TemplateApiRequest from(final RenderTemplateRequest request) {
        return new TemplateApiRequest(request);
    }
}

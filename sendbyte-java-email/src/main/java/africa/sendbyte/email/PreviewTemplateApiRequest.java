package africa.sendbyte.email;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

final class PreviewTemplateApiRequest {
    @JsonProperty("variables")
    private final Map<String, Object> variables;

    private PreviewTemplateApiRequest(final PreviewTemplateRequest request) {
        this.variables = request.variables();
    }

    static PreviewTemplateApiRequest from(final PreviewTemplateRequest request) {
        return new PreviewTemplateApiRequest(request);
    }
}

package africa.sendbyte.admin;

import com.fasterxml.jackson.annotation.JsonProperty;

final class ApiKeyApiRequest {
    @JsonProperty("name")
    private final String name;

    @JsonProperty("scope")
    private final String scope;

    @JsonProperty("mode")
    private final String mode;

    private ApiKeyApiRequest(final String name, final ApiKeyScope scope, final ApiKeyMode mode) {
        this.name = name;
        this.scope = scope == null ? null : scope.wireValue();
        this.mode = mode == null ? null : mode.wireValue();
    }

    static ApiKeyApiRequest from(final CreateApiKeyRequest request) {
        return new ApiKeyApiRequest(request.name(), request.scope(), request.mode());
    }

    static ApiKeyApiRequest rename(final String name) {
        return new ApiKeyApiRequest(CreateApiKeyRequest.requireName(name), null, null);
    }
}

package africa.sendbyte.admin;

import africa.sendbyte.exception.SerializationException;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

final class ApiKeyApiResponse {
    @JsonProperty("id")
    public String id;

    @JsonProperty("name")
    public String name;

    @JsonProperty("prefix")
    public String prefix;

    @JsonProperty("last4")
    public String last4;

    @JsonProperty("scope")
    public String scope;

    @JsonProperty("mode")
    public String mode;

    @JsonProperty("last_used_at")
    public String lastUsedAt;

    @JsonProperty("revoked")
    public boolean revoked;

    @JsonProperty("created_at")
    public String createdAt;

    @JsonProperty("key")
    public String key;

    ApiKey toApiKey() {
        return new ApiKey(
                this.id,
                this.name,
                this.prefix,
                this.last4,
                this.scope,
                this.mode,
                parse(this.lastUsedAt),
                this.revoked,
                parse(this.createdAt),
                this.key);
    }

    private static OffsetDateTime parse(final String value) {
        try {
            return value == null ? null : OffsetDateTime.parse(value);
        } catch (final DateTimeParseException exception) {
            throw new SerializationException("Could not decode an API key timestamp", exception);
        }
    }
}

package africa.sendbyte.admin;

import java.time.OffsetDateTime;

/** API key metadata returned by the admin API. */
public final class ApiKey {
    private final String id;
    private final String name;
    private final String prefix;
    private final String last4;
    private final ApiKeyScope scope;
    private final String rawScope;
    private final ApiKeyMode mode;
    private final String rawMode;
    private final OffsetDateTime lastUsedAt;
    private final boolean revoked;
    private final OffsetDateTime createdAt;
    private final String key;

    ApiKey(
            final String id,
            final String name,
            final String prefix,
            final String last4,
            final String rawScope,
            final String rawMode,
            final OffsetDateTime lastUsedAt,
            final boolean revoked,
            final OffsetDateTime createdAt,
            final String key) {
        this.id = id;
        this.name = name;
        this.prefix = prefix;
        this.last4 = last4;
        this.scope = ApiKeyScope.fromWire(rawScope);
        this.rawScope = rawScope;
        this.mode = ApiKeyMode.fromWire(rawMode);
        this.rawMode = rawMode;
        this.lastUsedAt = lastUsedAt;
        this.revoked = revoked;
        this.createdAt = createdAt;
        this.key = key;
    }

    public String id() {
        return this.id;
    }

    public String name() {
        return this.name;
    }

    public String prefix() {
        return this.prefix;
    }

    public String last4() {
        return this.last4;
    }

    public ApiKeyScope scope() {
        return this.scope;
    }

    public String rawScope() {
        return this.rawScope;
    }

    public ApiKeyMode mode() {
        return this.mode;
    }

    public String rawMode() {
        return this.rawMode;
    }

    public OffsetDateTime lastUsedAt() {
        return this.lastUsedAt;
    }

    public boolean revoked() {
        return this.revoked;
    }

    public OffsetDateTime createdAt() {
        return this.createdAt;
    }

    public String key() {
        return this.key;
    }

    @Override
    public String toString() {
        return "ApiKey{"
                + "id='"
                + this.id
                + '\''
                + ", name='"
                + this.name
                + '\''
                + ", prefix='"
                + this.prefix
                + '\''
                + ", last4='"
                + this.last4
                + '\''
                + ", scope="
                + this.scope
                + ", mode="
                + this.mode
                + ", revoked="
                + this.revoked
                + ", key=[REDACTED]"
                + '}';
    }
}

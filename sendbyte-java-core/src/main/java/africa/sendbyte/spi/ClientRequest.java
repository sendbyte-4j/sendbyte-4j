package africa.sendbyte.spi;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Immutable request description used by SendByte product modules. */
public final class ClientRequest {
    private final String operation;
    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private final Object body;

    private ClientRequest(final Builder builder) {
        this.operation = builder.operation;
        this.method = builder.method;
        this.path = builder.path;
        this.headers = Collections.unmodifiableMap(new LinkedHashMap<>(builder.headers));
        this.body = builder.body;
    }

    public static Builder builder(final String operation, final String method, final String path) {
        return new Builder(operation, method, path);
    }

    public String operation() {
        return this.operation;
    }

    public String method() {
        return this.method;
    }

    public String path() {
        return this.path;
    }

    public Map<String, String> headers() {
        return this.headers;
    }

    public Object body() {
        return this.body;
    }

    public static final class Builder {
        private final String operation;
        private final String method;
        private final String path;
        private final Map<String, String> headers = new LinkedHashMap<>();
        private Object body;

        private Builder(final String operation, final String method, final String path) {
            this.operation = requireText(operation, "operation");
            this.method = requireText(method, "method");
            this.path = requirePath(path);
        }

        public Builder header(final String name, final String value) {
            this.headers.put(requireText(name, "header name"), requireText(value, "header value"));
            return this;
        }

        public Builder body(final Object body) {
            this.body = body;
            return this;
        }

        public ClientRequest build() {
            return new ClientRequest(this);
        }

        private static String requirePath(final String value) {
            final String path = requireText(value, "path");
            if (!path.startsWith("/v1/")) {
                throw new IllegalArgumentException("path must start with /v1/");
            }
            return path;
        }

        private static String requireText(final String value, final String name) {
            Objects.requireNonNull(value, name);
            if (value.trim().isEmpty()) {
                throw new IllegalArgumentException(name + " must not be blank");
            }
            return value;
        }
    }
}

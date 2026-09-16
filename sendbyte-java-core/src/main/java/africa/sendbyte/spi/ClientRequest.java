package africa.sendbyte.spi;

import africa.sendbyte.RequestOptions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/** Immutable request description used by SendByte product modules. */
public final class ClientRequest {
    private final String operation;
    private final String method;
    private final String path;
    private final Map<String, List<String>> headers;
    private final RequestOptions options;
    private final boolean idempotencyProtected;
    private final Object body;

    private ClientRequest(final Builder builder) {
        this.operation = builder.operation;
        this.method = builder.method;
        this.path = builder.encodedPath();
        this.headers = immutableHeaders(builder.headers);
        this.options = builder.options;
        this.idempotencyProtected = builder.idempotencyProtected;
        this.body = builder.body;
    }

    public static Builder builder(final String operation, final String method) {
        return new Builder(operation, method);
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

    public Map<String, List<String>> headers() {
        return this.headers;
    }

    public RequestOptions options() {
        return this.options;
    }

    public boolean idempotencyProtected() {
        return this.idempotencyProtected;
    }

    public Object body() {
        return this.body;
    }

    public static final class Builder {
        private final String operation;
        private final String method;
        private final String path;
        private final List<String> pathSegments = new ArrayList<>();
        private final Map<String, List<String>> headers = caseInsensitiveHeaders();
        private final List<QueryParameter> queryParameters = new ArrayList<>();
        private RequestOptions options = RequestOptions.DEFAULT;
        private boolean idempotencyProtected;
        private Object body;

        private Builder(final String operation, final String method) {
            this.operation = requireText(operation, "operation");
            this.method = requireText(method, "method");
            this.path = null;
        }

        private Builder(final String operation, final String method, final String path) {
            this.operation = requireText(operation, "operation");
            this.method = requireText(method, "method");
            this.path = requirePath(path);
        }

        public Builder header(final String name, final String value) {
            this.headers
                    .computeIfAbsent(requireText(name, "header name"), ignored -> new ArrayList<>())
                    .add(requireText(value, "header value"));
            return this;
        }

        public Builder pathSegment(final String value) {
            if (this.path != null) {
                throw new IllegalStateException(
                        "pathSegment cannot be used when a literal path was supplied");
            }
            this.pathSegments.add(encode(requireText(value, "path segment")));
            return this;
        }

        public Builder queryParameter(final String name, final String value) {
            if (this.path != null) {
                throw new IllegalStateException(
                        "queryParameter cannot be used when a literal path was supplied");
            }
            this.queryParameters.add(
                    new QueryParameter(
                            requireText(name, "query parameter name"),
                            requireText(value, "query parameter value")));
            return this;
        }

        public Builder body(final Object body) {
            this.body = body;
            return this;
        }

        public Builder options(final RequestOptions options) {
            this.options = Objects.requireNonNull(options, "options");
            return this;
        }

        public Builder idempotencyProtected(final boolean idempotencyProtected) {
            this.idempotencyProtected = idempotencyProtected;
            return this;
        }

        public ClientRequest build() {
            return new ClientRequest(this);
        }

        private static String requirePath(final String value) {
            final String path = requireText(value, "path");
            if (!path.startsWith("/v1/") || path.contains("?") || path.contains("#")) {
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

        private String encodedPath() {
            if (this.path != null) {
                return this.path;
            }
            if (this.pathSegments.isEmpty() || !"v1".equals(this.pathSegments.get(0))) {
                throw new IllegalArgumentException("path must start with /v1/");
            }

            final StringBuilder builder = new StringBuilder();
            for (final String segment : this.pathSegments) {
                builder.append('/').append(segment);
            }
            for (int index = 0; index < this.queryParameters.size(); index++) {
                final QueryParameter parameter = this.queryParameters.get(index);
                builder.append(index == 0 ? '?' : '&')
                        .append(encode(parameter.name))
                        .append('=')
                        .append(encode(parameter.value));
            }
            return builder.toString();
        }
    }

    private static Map<String, List<String>> immutableHeaders(
            final Map<String, List<String>> headers) {
        final Map<String, List<String>> copy = caseInsensitiveHeaders();
        headers.forEach((name, values) -> copy.put(name, List.copyOf(values)));
        return Collections.unmodifiableMap(copy);
    }

    private static Map<String, List<String>> caseInsensitiveHeaders() {
        return new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    }

    private static String encode(final String value) {
        final StringBuilder builder = new StringBuilder();
        final byte[] bytes = value.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        for (final byte current : bytes) {
            final int unsigned = current & 0xff;
            if (isUnreserved(unsigned)) {
                builder.append((char) unsigned);
            } else {
                builder.append('%');
                final String hex = Integer.toHexString(unsigned).toUpperCase(java.util.Locale.ROOT);
                if (hex.length() == 1) {
                    builder.append('0');
                }
                builder.append(hex);
            }
        }
        return builder.toString();
    }

    private static boolean isUnreserved(final int value) {
        return (value >= 'a' && value <= 'z')
                || (value >= 'A' && value <= 'Z')
                || (value >= '0' && value <= '9')
                || value == '-'
                || value == '.'
                || value == '_'
                || value == '~';
    }

    private static final class QueryParameter {
        private final String name;
        private final String value;

        private QueryParameter(final String name, final String value) {
            this.name = name;
            this.value = value;
        }
    }
}

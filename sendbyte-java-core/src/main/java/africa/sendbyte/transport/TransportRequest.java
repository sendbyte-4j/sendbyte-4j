package africa.sendbyte.transport;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Immutable, transport-neutral HTTP request. */
public final class TransportRequest {
    private final String method;
    private final URI uri;
    private final Map<String, List<String>> headers;
    private final byte[] body;
    private final Duration timeout;

    public TransportRequest(
            final String method,
            final URI uri,
            final Map<String, List<String>> headers,
            final byte[] body,
            final Duration timeout) {
        this.method = Objects.requireNonNull(method, "method");
        this.uri = Objects.requireNonNull(uri, "uri");
        final Map<String, List<String>> copiedHeaders = new LinkedHashMap<>();
        headers.forEach((name, values) -> copiedHeaders.put(name, List.copyOf(values)));

        this.headers = Collections.unmodifiableMap(copiedHeaders);
        this.body = body == null ? new byte[0] : body.clone();
        this.timeout = Objects.requireNonNull(timeout, "timeout");
    }

    public String method() {
        return this.method;
    }

    public URI uri() {
        return this.uri;
    }

    public Map<String, List<String>> headers() {
        return this.headers;
    }

    public byte[] body() {
        return this.body.clone();
    }

    public Duration timeout() {
        return this.timeout;
    }
}

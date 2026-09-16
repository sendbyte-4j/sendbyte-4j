package africa.sendbyte.transport;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

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
        final Map<String, List<String>> copiedHeaders = caseInsensitiveHeaders();
        headers.forEach(
                (name, values) ->
                        copiedHeaders
                                .computeIfAbsent(
                                        Objects.requireNonNull(name, "header name"),
                                        ignored -> new ArrayList<>())
                                .addAll(List.copyOf(values)));

        this.headers = immutableHeaders(copiedHeaders);
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

    private static Map<String, List<String>> caseInsensitiveHeaders() {
        return new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    }

    private static Map<String, List<String>> immutableHeaders(
            final Map<String, List<String>> headers) {
        final Map<String, List<String>> copy = caseInsensitiveHeaders();
        headers.forEach((name, values) -> copy.put(name, List.copyOf(values)));
        return Collections.unmodifiableMap(copy);
    }
}

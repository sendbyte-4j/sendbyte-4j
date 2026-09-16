package africa.sendbyte.transport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/** Immutable, transport-neutral HTTP response. */
public final class TransportResponse {
    private final int statusCode;
    private final Map<String, List<String>> headers;
    private final byte[] body;

    public TransportResponse(
            final int statusCode, final Map<String, List<String>> headers, final byte[] body) {
        this.statusCode = statusCode;
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
    }

    public int statusCode() {
        return this.statusCode;
    }

    public Map<String, List<String>> headers() {
        return this.headers;
    }

    public String firstHeader(final String name) {
        return this.headers.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(name))
                .flatMap(entry -> entry.getValue().stream())
                .findFirst()
                .orElse(null);
    }

    public byte[] body() {
        return this.body.clone();
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

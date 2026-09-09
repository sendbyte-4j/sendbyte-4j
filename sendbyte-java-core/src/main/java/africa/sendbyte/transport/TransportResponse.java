package africa.sendbyte.transport;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Immutable, transport-neutral HTTP response. */
public final class TransportResponse {
    private final int statusCode;
    private final Map<String, List<String>> headers;
    private final byte[] body;

    public TransportResponse(
            final int statusCode, final Map<String, List<String>> headers, final byte[] body) {
        this.statusCode = statusCode;
        final Map<String, List<String>> copiedHeaders = new LinkedHashMap<>();
        headers.forEach((name, values) -> copiedHeaders.put(name, List.copyOf(values)));

        this.headers = Collections.unmodifiableMap(copiedHeaders);
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
}

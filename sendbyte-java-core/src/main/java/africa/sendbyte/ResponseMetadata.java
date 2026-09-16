package africa.sendbyte;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/** Transport-neutral response metadata exposed by detailed SDK calls. */
public final class ResponseMetadata {
    private final int statusCode;
    private final Map<String, List<String>> headers;
    private final String requestId;

    public ResponseMetadata(final int statusCode, final Map<String, List<String>> headers) {
        this.statusCode = statusCode;
        final Map<String, List<String>> copy = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        Objects.requireNonNull(headers, "headers")
                .forEach((name, values) -> copy.put(name, List.copyOf(values)));
        this.headers = Collections.unmodifiableMap(copy);
        this.requestId = this.firstHeader("x-request-id");
    }

    public int statusCode() {
        return this.statusCode;
    }

    public Map<String, List<String>> headers() {
        return this.headers;
    }

    public String firstHeader(final String name) {
        final List<String> values = this.headers.get(name);
        return values == null || values.isEmpty() ? null : values.get(0);
    }

    public String requestId() {
        return this.requestId;
    }
}

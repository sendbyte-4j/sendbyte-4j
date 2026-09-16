package africa.sendbyte.exception;

import africa.sendbyte.RequestDiagnostics;
import java.time.Duration;

/** The API rate limit was exceeded. */
public final class RateLimitException extends ApiException {
    private static final long serialVersionUID = 1L;
    private final Duration retryAfter;

    public RateLimitException(
            final String message,
            final int statusCode,
            final String code,
            final String documentationUrl,
            final String requestId,
            final Duration retryAfter) {
        super(message, statusCode, code, documentationUrl, requestId);
        this.retryAfter = retryAfter;
    }

    public RateLimitException(
            final String message,
            final int statusCode,
            final String code,
            final String documentationUrl,
            final String requestId,
            final Duration retryAfter,
            final RequestDiagnostics diagnostics) {
        super(message, statusCode, code, documentationUrl, requestId, diagnostics);
        this.retryAfter = retryAfter;
    }

    public Duration retryAfter() {
        return this.retryAfter;
    }
}

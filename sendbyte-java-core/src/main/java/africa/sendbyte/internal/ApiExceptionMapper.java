package africa.sendbyte.internal;

import africa.sendbyte.FailureCategory;
import africa.sendbyte.RequestDiagnostics;
import africa.sendbyte.exception.ApiException;
import africa.sendbyte.exception.AuthenticationException;
import africa.sendbyte.exception.AuthorizationException;
import africa.sendbyte.exception.ConflictException;
import africa.sendbyte.exception.NotFoundException;
import africa.sendbyte.exception.RateLimitException;
import africa.sendbyte.exception.ValidationException;
import africa.sendbyte.spi.ClientRequest;
import africa.sendbyte.transport.TransportResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;

final class ApiExceptionMapper {
    private final JacksonJsonCodec json;
    private final RetryPlanner retryPlanner;
    private final DiagnosticsReporter diagnosticsReporter;

    ApiExceptionMapper(
            final JacksonJsonCodec json,
            final RetryPlanner retryPlanner,
            final DiagnosticsReporter diagnosticsReporter) {
        this.json = json;
        this.retryPlanner = retryPlanner;
        this.diagnosticsReporter = diagnosticsReporter;
    }

    ApiException map(
            final ClientRequest request,
            final TransportResponse response,
            final ExecutionState state,
            final boolean retryable) {
        ErrorEnvelope envelope = null;
        if (response.bodyLength() > 0) {
            try {
                envelope = this.json.read(response.body(), ErrorEnvelope.class);
            } catch (final RuntimeException ignored) {
                // Preserve a useful HTTP failure even when a proxy returned non-contract JSON.
            }
        }

        final ErrorBody error = envelope == null ? null : envelope.error;
        final String code = error == null ? null : error.code;
        final String message =
                Sanitizer.sanitize(
                        error == null || error.message == null
                                ? "SendByte API request failed with HTTP " + response.statusCode()
                                : error.message);
        final String docsUrl = error == null ? null : error.docsUrl;
        final String requestId = response.firstHeader("x-request-id");
        final RequestDiagnostics diagnostics =
                this.diagnosticsReporter.create(
                        request,
                        state,
                        response.statusCode(),
                        requestId,
                        FailureCategory.API,
                        retryable);

        final ApiException exception =
                this.toApiException(
                        message,
                        response.statusCode(),
                        code,
                        docsUrl,
                        requestId,
                        this.retryPlanner.retryAfter(response.firstHeader("Retry-After")),
                        diagnostics);
        this.diagnosticsReporter.observe(diagnostics);
        return exception;
    }

    private ApiException toApiException(
            final String message,
            final int statusCode,
            final String code,
            final String docsUrl,
            final String requestId,
            final Duration retryAfter,
            final RequestDiagnostics diagnostics) {
        if ("validation_error".equals(code) || statusCode == 422) {
            return new ValidationException(
                    message, statusCode, code, docsUrl, requestId, diagnostics);
        }
        if ("authentication_error".equals(code) || statusCode == 401) {
            return new AuthenticationException(
                    message, statusCode, code, docsUrl, requestId, diagnostics);
        }
        if ("authorization_error".equals(code) || statusCode == 403) {
            return new AuthorizationException(
                    message, statusCode, code, docsUrl, requestId, diagnostics);
        }
        if ("not_found".equals(code) || statusCode == 404) {
            return new NotFoundException(
                    message, statusCode, code, docsUrl, requestId, diagnostics);
        }
        if ("idempotency_conflict".equals(code) || statusCode == 409) {
            return new ConflictException(
                    message, statusCode, code, docsUrl, requestId, diagnostics);
        }
        if ("rate_limit_exceeded".equals(code) || statusCode == 429) {
            return new RateLimitException(
                    message, statusCode, code, docsUrl, requestId, retryAfter, diagnostics);
        }
        return new ApiException(message, statusCode, code, docsUrl, requestId, diagnostics);
    }

    public static final class ErrorEnvelope {
        public ErrorBody error;
    }

    public static final class ErrorBody {
        public String code;
        public String message;

        @JsonProperty("docs_url")
        public String docsUrl;
    }
}

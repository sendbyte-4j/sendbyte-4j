package africa.sendbyte.internal;

import africa.sendbyte.CredentialsProvider;
import africa.sendbyte.exception.ApiException;
import africa.sendbyte.exception.AuthenticationException;
import africa.sendbyte.exception.AuthorizationException;
import africa.sendbyte.exception.ConflictException;
import africa.sendbyte.exception.ConnectionException;
import africa.sendbyte.exception.NotFoundException;
import africa.sendbyte.exception.RateLimitException;
import africa.sendbyte.exception.ValidationException;
import africa.sendbyte.spi.ClientRequest;
import africa.sendbyte.spi.ClientRuntime;
import africa.sendbyte.transport.HttpTransport;
import africa.sendbyte.transport.TransportRequest;
import africa.sendbyte.transport.TransportResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;

public final class DefaultClientRuntime implements ClientRuntime {
    private final URI baseUri;
    private final CredentialsProvider credentialsProvider;
    private final HttpTransport transport;
    private final Duration requestTimeout;
    private final String userAgent;
    private final AtomicBoolean closed;
    private final JacksonJsonCodec json = new JacksonJsonCodec();

    public DefaultClientRuntime(
            final URI baseUri,
            final CredentialsProvider credentialsProvider,
            final HttpTransport transport,
            final Duration requestTimeout,
            final String userAgent,
            final AtomicBoolean closed) {
        this.baseUri = baseUri;
        this.credentialsProvider = credentialsProvider;
        this.transport = transport;
        this.requestTimeout = requestTimeout;
        this.userAgent = userAgent;
        this.closed = closed;
    }

    @Override
    public <T> T execute(final ClientRequest request, final Class<T> responseType) {
        this.ensureOpen();

        try {
            return this.decode(
                    this.transport.execute(this.toTransportRequest(request)), responseType);
        } catch (final InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ConnectionException("Interrupted while calling the SendByte API", exception);
        } catch (final HttpTimeoutException exception) {
            throw new ConnectionException("The SendByte API request timed out", exception);
        } catch (final java.io.IOException exception) {
            throw new ConnectionException("Could not reach the SendByte API", exception);
        }
    }

    @Override
    public <T> CompletableFuture<T> executeAsync(
            final ClientRequest request, final Class<T> responseType) {
        this.ensureOpen();

        final TransportRequest transportRequest;
        try {
            transportRequest = this.toTransportRequest(request);
        } catch (final RuntimeException failure) {
            final CompletableFuture<T> failed = new CompletableFuture<>();
            failed.completeExceptionally(failure);
            return failed;
        }

        final CompletableFuture<TransportResponse> source =
                this.transport.executeAsync(transportRequest);
        final CompletableFuture<T> result = new CompletableFuture<>();

        source.whenComplete(
                (response, failure) -> {
                    if (failure != null) {
                        final Throwable cause = unwrap(failure);
                        result.completeExceptionally(
                                cause instanceof ApiException
                                        ? cause
                                        : new ConnectionException(
                                                "Could not reach the SendByte API", cause));
                        return;
                    }

                    try {
                        result.complete(this.decode(response, responseType));
                    } catch (final RuntimeException decodingFailure) {
                        result.completeExceptionally(decodingFailure);
                    }
                });

        result.whenComplete(
                (ignored, failure) -> {
                    if (result.isCancelled()) {
                        source.cancel(true);
                    }
                });
        return result;
    }

    private TransportRequest toTransportRequest(final ClientRequest request) {
        final byte[] body = request.body() == null ? new byte[0] : this.json.write(request.body());
        final Map<String, List<String>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        request.headers().forEach((name, values) -> headers.put(name, List.copyOf(values)));
        headers.put("Authorization", Collections.singletonList("Bearer " + this.currentApiKey()));
        headers.put("Accept", Collections.singletonList("application/json"));
        headers.put("User-Agent", Collections.singletonList(this.userAgent));

        if (request.body() != null) {
            headers.put("Content-Type", Collections.singletonList("application/json"));
        }

        return new TransportRequest(
                request.method(),
                this.baseUri.resolve(request.path()),
                headers,
                body,
                this.requestTimeout);
    }

    private String currentApiKey() {
        final String apiKey = this.credentialsProvider.apiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("The credentials provider returned a blank API key");
        }

        return apiKey;
    }

    private <T> T decode(final TransportResponse response, final Class<T> responseType) {
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            if (response.body().length == 0) {
                return null;
            }
            return this.json.read(response.body(), responseType);
        }

        throw this.apiFailure(response);
    }

    private ApiException apiFailure(final TransportResponse response) {
        ErrorEnvelope envelope = null;
        try {
            envelope = this.json.read(response.body(), ErrorEnvelope.class);
        } catch (final RuntimeException ignored) {
            // Preserve a useful HTTP failure even when a proxy returned non-contract JSON.
        }

        final ErrorBody error = envelope == null ? null : envelope.error;
        final String code = error == null ? null : error.code;
        final String message =
                error == null || error.message == null
                        ? "SendByte API request failed with HTTP " + response.statusCode()
                        : error.message;
        final String docsUrl = error == null ? null : error.docsUrl;
        final String requestId = response.firstHeader("x-request-id");

        if ("validation_error".equals(code) || response.statusCode() == 422) {
            return new ValidationException(
                    message, response.statusCode(), code, docsUrl, requestId);
        }
        if ("authentication_error".equals(code) || response.statusCode() == 401) {
            return new AuthenticationException(
                    message, response.statusCode(), code, docsUrl, requestId);
        }
        if ("authorization_error".equals(code) || response.statusCode() == 403) {
            return new AuthorizationException(
                    message, response.statusCode(), code, docsUrl, requestId);
        }
        if ("not_found".equals(code) || response.statusCode() == 404) {
            return new NotFoundException(message, response.statusCode(), code, docsUrl, requestId);
        }
        if ("idempotency_conflict".equals(code) || response.statusCode() == 409) {
            return new ConflictException(message, response.statusCode(), code, docsUrl, requestId);
        }
        if ("rate_limit_exceeded".equals(code) || response.statusCode() == 429) {
            return new RateLimitException(
                    message,
                    response.statusCode(),
                    code,
                    docsUrl,
                    requestId,
                    retryAfter(response.firstHeader("Retry-After")));
        }
        return new ApiException(message, response.statusCode(), code, docsUrl, requestId);
    }

    private static Duration retryAfter(final String value) {
        if (value == null) {
            return null;
        }
        try {
            return Duration.ofSeconds(Long.parseLong(value.trim()));
        } catch (final NumberFormatException ignored) {
            return null;
        }
    }

    private static Throwable unwrap(final Throwable failure) {
        Throwable current = failure;
        while ((current instanceof CompletionException
                        || current instanceof java.util.concurrent.ExecutionException)
                && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    private void ensureOpen() {
        if (this.closed.get()) {
            throw new IllegalStateException("SendByteClient is closed");
        }
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

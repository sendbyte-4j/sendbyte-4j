package africa.sendbyte.internal;

import africa.sendbyte.FailureCategory;
import africa.sendbyte.RequestDiagnostics;
import africa.sendbyte.exception.SendByteCancellationException;
import africa.sendbyte.exception.SendByteException;
import africa.sendbyte.exception.SendByteTimeoutException;
import africa.sendbyte.spi.ClientRequest;
import africa.sendbyte.transport.TransportRequest;
import africa.sendbyte.transport.TransportResponse;
import java.time.Duration;

final class ExecutionSupport {
    private final Duration requestTimeout;
    private final Ticker ticker;
    private final Sleeper sleeper;
    private final ClientLifecycle lifecycle;
    private final JacksonJsonCodec json;
    private final TransportRequestFactory requestFactory;
    private final DiagnosticsReporter diagnosticsReporter;
    private final RetryPlanner retryPlanner;
    private final ApiExceptionMapper apiExceptionMapper;
    private final ResponseProcessor responseProcessor;

    ExecutionSupport(
            final Duration requestTimeout,
            final Ticker ticker,
            final Sleeper sleeper,
            final ClientLifecycle lifecycle,
            final JacksonJsonCodec json,
            final TransportRequestFactory requestFactory,
            final DiagnosticsReporter diagnosticsReporter,
            final RetryPlanner retryPlanner,
            final ApiExceptionMapper apiExceptionMapper,
            final ResponseProcessor responseProcessor) {
        this.requestTimeout = requestTimeout;
        this.ticker = ticker;
        this.sleeper = sleeper;
        this.lifecycle = lifecycle;
        this.json = json;
        this.requestFactory = requestFactory;
        this.diagnosticsReporter = diagnosticsReporter;
        this.retryPlanner = retryPlanner;
        this.apiExceptionMapper = apiExceptionMapper;
        this.responseProcessor = responseProcessor;
    }

    ExecutionState newState(final ClientRequest request) {
        return new ExecutionState(request, this.requestTimeout, this.ticker);
    }

    TransportRequest transportRequest(final ClientRequest request, final ExecutionState state) {
        return requestFactory.create(
                request, state.encodedBody(this::encodeBody), state.remaining(this.ticker));
    }

    private byte[] encodeBody(final ClientRequest request) {
        return request.body() == null ? null : this.json.write(request.body());
    }

    <T> africa.sendbyte.DetailedResponse<T> success(
            final ClientRequest request,
            final TransportResponse response,
            final Class<T> responseType,
            final ExecutionState state) {
        return responseProcessor.success(request, response, responseType, state);
    }

    RetryAttempt retryForResponse(
            final ClientRequest request,
            final TransportResponse response,
            final ExecutionState state) {
        responseProcessor.enforceBodyLimit(request, response, state);
        if (!retryPlanner.canRetryResponse(request, response.statusCode())) {
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return RetryAttempt.none();
            }
            throw apiExceptionMapper.map(request, response, state, false);
        }

        final Duration delay =
                retryPlanner.delay(response.firstHeader("Retry-After"), state.attemptCount());
        if (retryPlanner.canRetryAttempt(request, state, delay, this.ticker)) {
            state.addRetryDelay(delay);
            return RetryAttempt.retry(delay);
        }

        this.throwIfRetryBlockedByDeadline(request, state, response, delay);

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return RetryAttempt.none();
        }
        throw apiExceptionMapper.map(request, response, state, false);
    }

    RetryAttempt retryForFailure(
            final ClientRequest request,
            final ExecutionState state,
            final boolean retryableFailure) {
        if (!retryPlanner.canRetryFailure(request, retryableFailure)) {
            return RetryAttempt.none();
        }

        final Duration delay = retryPlanner.delay(null, state.attemptCount());
        if (!retryPlanner.canRetryAttempt(request, state, delay, this.ticker)) {
            this.throwIfRetryBlockedByDeadline(request, state, null, delay);
            return RetryAttempt.none();
        }

        state.addRetryDelay(delay);
        return RetryAttempt.retry(delay);
    }

    void sleepBeforeRetry(final Duration delay, final ExecutionState state) {
        this.ensureDeadline(state);
        try {
            sleeper.sleep(delay);
        } catch (final InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new SendByteCancellationException(
                    "The SendByte API request was canceled during retry backoff",
                    this.diagnostics(
                            state.request(),
                            state,
                            null,
                            null,
                            FailureCategory.CANCELLATION,
                            false));
        }
        this.ensureDeadline(state);
    }

    <T extends SendByteException> T failure(
            final ClientRequest request,
            final T exception,
            final ExecutionState state,
            final TransportResponse response,
            final FailureCategory category,
            final boolean retryable) {
        this.observe(this.diagnostics(request, state, response, category, retryable));
        return exception;
    }

    RequestDiagnostics diagnostics(
            final ClientRequest request,
            final ExecutionState state,
            final Integer statusCode,
            final String requestId,
            final FailureCategory category,
            final boolean retryable) {
        return this.diagnosticsReporter.create(
                request, state, statusCode, requestId, category, retryable);
    }

    void ensureOpen() {
        this.lifecycle.ensureOpen();
    }

    void ensureDeadline(final ExecutionState state) {
        if (state.remaining(this.ticker).isZero() || state.remaining(this.ticker).isNegative()) {
            final RequestDiagnostics diagnostics =
                    this.diagnostics(
                            state.request(), state, null, null, FailureCategory.TIMEOUT, false);
            this.observe(diagnostics);
            throw new SendByteTimeoutException(
                    "The SendByte API request exceeded the configured total deadline", diagnostics);
        }
    }

    private void throwIfRetryBlockedByDeadline(
            final ClientRequest request,
            final ExecutionState state,
            final TransportResponse response,
            final Duration delay) {
        if (retryPlanner.retryBlockedByDeadline(request, state, delay, this.ticker)) {
            final RequestDiagnostics diagnostics =
                    this.diagnostics(
                            request,
                            state,
                            response == null ? null : response.statusCode(),
                            response == null ? null : response.firstHeader("x-request-id"),
                            FailureCategory.TIMEOUT,
                            false);
            this.observe(diagnostics);
            throw new SendByteTimeoutException(
                    "The SendByte API request exceeded the configured total deadline", diagnostics);
        }
    }

    private RequestDiagnostics diagnostics(
            final ClientRequest request,
            final ExecutionState state,
            final TransportResponse response,
            final FailureCategory category,
            final boolean retryable) {
        return diagnosticsReporter.create(request, state, response, category, retryable);
    }

    private void observe(final RequestDiagnostics diagnostics) {
        diagnosticsReporter.observe(diagnostics);
    }
}

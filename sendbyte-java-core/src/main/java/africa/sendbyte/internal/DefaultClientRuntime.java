package africa.sendbyte.internal;

import africa.sendbyte.CredentialsProvider;
import africa.sendbyte.DetailedResponse;
import africa.sendbyte.FailureCategory;
import africa.sendbyte.RequestObserver;
import africa.sendbyte.RetryPolicy;
import africa.sendbyte.exception.ConnectionException;
import africa.sendbyte.exception.SendByteCancellationException;
import africa.sendbyte.exception.SendByteException;
import africa.sendbyte.exception.SendByteTimeoutException;
import africa.sendbyte.exception.SerializationException;
import africa.sendbyte.spi.ClientRequest;
import africa.sendbyte.spi.ClientRuntime;
import africa.sendbyte.transport.HttpTransport;
import africa.sendbyte.transport.TransportResponse;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpTimeoutException;
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.DoubleSupplier;

public final class DefaultClientRuntime implements ClientRuntime {
    private final HttpTransport transport;
    private final ScheduledExecutorService executor;
    private final ClientLifecycle lifecycle;
    private final ExecutionSupport support;

    public DefaultClientRuntime(
            final URI baseUri,
            final CredentialsProvider credentialsProvider,
            final HttpTransport transport,
            final Duration requestTimeout,
            final String userAgent,
            final RetryPolicy retryPolicy,
            final RequestObserver requestObserver,
            final long maxResponseBodyBytes,
            final ScheduledExecutorService executor,
            final ClientLifecycle lifecycle) {
        this(
                baseUri,
                credentialsProvider,
                transport,
                requestTimeout,
                userAgent,
                retryPolicy,
                requestObserver,
                maxResponseBodyBytes,
                executor,
                Clock.systemUTC(),
                System::nanoTime,
                delay -> TimeUnit.NANOSECONDS.sleep(delay.toNanos()),
                Math::random,
                lifecycle);
    }

    DefaultClientRuntime(
            final URI baseUri,
            final CredentialsProvider credentialsProvider,
            final HttpTransport transport,
            final Duration requestTimeout,
            final String userAgent,
            final RetryPolicy retryPolicy,
            final RequestObserver requestObserver,
            final long maxResponseBodyBytes,
            final ScheduledExecutorService executor,
            final Clock clock,
            final Ticker ticker,
            final Sleeper sleeper,
            final DoubleSupplier random,
            final ClientLifecycle lifecycle) {
        final JacksonJsonCodec json = new JacksonJsonCodec();
        final TransportRequestFactory requestFactory =
                new TransportRequestFactory(baseUri, credentialsProvider, userAgent);
        final DiagnosticsReporter diagnosticsReporter =
                new DiagnosticsReporter(ticker, requestObserver);
        final RetryPlanner retryPlanner = new RetryPlanner(retryPolicy, clock, random);

        this.transport = transport;
        this.executor = executor;
        this.lifecycle = lifecycle;
        this.support =
                new ExecutionSupport(
                        requestTimeout,
                        ticker,
                        sleeper,
                        lifecycle,
                        json,
                        requestFactory,
                        diagnosticsReporter,
                        retryPlanner,
                        new ApiExceptionMapper(json, retryPlanner, diagnosticsReporter),
                        new ResponseProcessor(json, maxResponseBodyBytes, diagnosticsReporter));
    }

    @Override
    public <T> T execute(final ClientRequest request, final Class<T> responseType) {
        return this.executeDetailed(request, responseType).value();
    }

    @Override
    public <T> DetailedResponse<T> executeDetailed(
            final ClientRequest request, final Class<T> responseType) {
        support.ensureOpen();
        final ExecutionState state = support.newState(request);

        while (true) {
            state.startAttempt();
            support.ensureOpen();
            support.ensureDeadline(state);

            try {
                final TransportResponse response =
                        transport.execute(support.transportRequest(request, state));
                final RetryAttempt retry = support.retryForResponse(request, response, state);
                if (retry.retry()) {
                    support.sleepBeforeRetry(retry.delay(), state);
                    continue;
                }

                return support.success(request, response, responseType, state);
            } catch (final InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw support.failure(
                        request,
                        new SendByteCancellationException(
                                "The SendByte API request was canceled",
                                support.diagnostics(
                                        request,
                                        state,
                                        null,
                                        null,
                                        FailureCategory.CANCELLATION,
                                        false)),
                        state,
                        null,
                        FailureCategory.CANCELLATION,
                        false);
            } catch (final HttpTimeoutException exception) {
                final RetryAttempt retry = support.retryForFailure(request, state, true);
                if (retry.retry()) {
                    support.sleepBeforeRetry(retry.delay(), state);
                    continue;
                }
                throw support.failure(
                        request,
                        new SendByteTimeoutException(
                                "The SendByte API request timed out",
                                exception,
                                support.diagnostics(
                                        request,
                                        state,
                                        null,
                                        null,
                                        FailureCategory.TIMEOUT,
                                        false)),
                        state,
                        null,
                        FailureCategory.TIMEOUT,
                        false);
            } catch (final IOException exception) {
                final RetryAttempt retry = support.retryForFailure(request, state, true);
                if (retry.retry()) {
                    support.sleepBeforeRetry(retry.delay(), state);
                    continue;
                }
                throw support.failure(
                        request,
                        new ConnectionException(
                                "Could not reach the SendByte API",
                                exception,
                                support.diagnostics(
                                        request,
                                        state,
                                        null,
                                        null,
                                        FailureCategory.CONNECTION,
                                        false)),
                        state,
                        null,
                        FailureCategory.CONNECTION,
                        false);
            } catch (final SerializationException exception) {
                throw support.failure(
                        request,
                        new SerializationException(
                                exception.getMessage(),
                                exception,
                                support.diagnostics(
                                        request,
                                        state,
                                        null,
                                        null,
                                        FailureCategory.SERIALIZATION,
                                        false)),
                        state,
                        null,
                        FailureCategory.SERIALIZATION,
                        false);
            } catch (final SendByteException exception) {
                throw exception;
            }
        }
    }

    @Override
    public <T> CompletableFuture<T> executeAsync(
            final ClientRequest request, final Class<T> responseType) {
        return AsyncSupport.map(
                this.executeDetailedAsync(request, responseType), DetailedResponse::value);
    }

    @Override
    public <T> CompletableFuture<DetailedResponse<T>> executeDetailedAsync(
            final ClientRequest request, final Class<T> responseType) {
        final AsyncOperation<T> operation = new AsyncOperation<>();
        lifecycle.register(operation);
        operation.whenComplete((ignored, failure) -> lifecycle.deregister(operation));
        this.dispatchAsync(request, responseType, support.newState(request), operation);
        return operation;
    }

    private <T> void dispatchAsync(
            final ClientRequest request,
            final Class<T> responseType,
            final ExecutionState state,
            final AsyncOperation<T> operation) {

        if (operation.isDone()) return;

        try {
            support.ensureOpen();
            support.ensureDeadline(state);
            state.startAttempt();
            final CompletableFuture<TransportResponse> current =
                    transport.executeAsync(support.transportRequest(request, state));
            operation.current(current);
            current.whenCompleteAsync(
                    (response, failure) -> {
                        if (operation.isDone()) {
                            current.cancel(true);
                            return;
                        }
                        if (failure != null) {
                            this.handleAsyncFailure(
                                    request,
                                    responseType,
                                    state,
                                    operation,
                                    AsyncSupport.unwrap(failure));
                        } else {
                            this.handleAsyncResponse(
                                    request, responseType, state, operation, response);
                        }
                    },
                    executor);
        } catch (final SendByteException exception) {
            operation.completeExceptionally(exception);
        } catch (final RuntimeException exception) {
            operation.completeExceptionally(
                    support.failure(
                            request,
                            new ConnectionException(
                                    "Could not reach the SendByte API",
                                    exception,
                                    support.diagnostics(
                                            request,
                                            state,
                                            null,
                                            null,
                                            FailureCategory.CONNECTION,
                                            false)),
                            state,
                            null,
                            FailureCategory.CONNECTION,
                            false));
        }
    }

    private <T> void handleAsyncResponse(
            final ClientRequest request,
            final Class<T> responseType,
            final ExecutionState state,
            final AsyncOperation<T> operation,
            final TransportResponse response) {
        try {
            final RetryAttempt retry = support.retryForResponse(request, response, state);
            if (retry.retry()) {
                operation.scheduled(
                        executor.schedule(
                                () -> this.dispatchAsync(request, responseType, state, operation),
                                retry.delay().toNanos(),
                                TimeUnit.NANOSECONDS));
                return;
            }
            operation.complete(support.success(request, response, responseType, state));
        } catch (final RuntimeException exception) {
            operation.completeExceptionally(exception);
        }
    }

    private <T> void handleAsyncFailure(
            final ClientRequest request,
            final Class<T> responseType,
            final ExecutionState state,
            final AsyncOperation<T> operation,
            final Throwable failure) {
        try {
            final RetryAttempt retry = support.retryForFailure(request, state, true);
            if (retry.retry()) {
                operation.scheduled(
                        executor.schedule(
                                () -> this.dispatchAsync(request, responseType, state, operation),
                                retry.delay().toNanos(),
                                TimeUnit.NANOSECONDS));
                return;
            }

            final FailureCategory category =
                    failure instanceof HttpTimeoutException
                            ? FailureCategory.TIMEOUT
                            : FailureCategory.CONNECTION;
            final SendByteException exception =
                    category == FailureCategory.TIMEOUT
                            ? new SendByteTimeoutException(
                                    "The SendByte API request timed out",
                                    failure,
                                    support.diagnostics(
                                            request, state, null, null, category, false))
                            : new ConnectionException(
                                    "Could not reach the SendByte API",
                                    failure,
                                    support.diagnostics(
                                            request, state, null, null, category, false));
            operation.completeExceptionally(
                    support.failure(request, exception, state, null, category, false));
        } catch (final RuntimeException exception) {
            operation.completeExceptionally(exception);
        }
    }
}

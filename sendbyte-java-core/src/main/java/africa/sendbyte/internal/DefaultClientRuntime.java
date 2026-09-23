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
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
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
        this.support.ensureOpen();
        final ExecutionState state = this.support.newState(request);

        while (true) {
            state.startAttempt();
            this.support.ensureOpen();
            this.support.ensureDeadline(state);

            try {
                final TransportResponse response =
                        this.transport.execute(this.support.transportRequest(request, state));
                final RetryAttempt retry = this.support.retryForResponse(request, response, state);
                if (retry.retry()) {
                    this.support.sleepBeforeRetry(retry.delay(), state);
                    continue;
                }

                return this.support.success(request, response, responseType, state);
            } catch (final InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw this.support.failure(
                        request,
                        new SendByteCancellationException(
                                "The SendByte API request was canceled",
                                this.support.diagnostics(
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
                final RetryAttempt retry = this.support.retryForFailure(request, state, true);
                if (retry.retry()) {
                    this.support.sleepBeforeRetry(retry.delay(), state);
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
                final RetryAttempt retry = this.support.retryForFailure(request, state, true);
                if (retry.retry()) {
                    this.support.sleepBeforeRetry(retry.delay(), state);
                    continue;
                }
                throw this.support.failure(
                        request,
                        new ConnectionException(
                                "Could not reach the SendByte API",
                                exception,
                                this.support.diagnostics(
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
                throw this.support.failure(
                        request,
                        new SerializationException(
                                exception.getMessage(),
                                exception,
                                this.support.diagnostics(
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
        final CompletableFuture<T> result = new CompletableFuture<>();
        final CompletableFuture<DetailedResponse<T>> source =
                this.executeDetailedAsync(request, responseType);
        source.whenCompleteAsync(
                (response, failure) -> {
                    if (failure != null) {
                        result.completeExceptionally(unwrap(failure));
                    } else {
                        result.complete(response.value());
                    }
                },
                this.executor);
        result.whenComplete(
                (ignored, failure) -> {
                    if (result.isCancelled()) {
                        source.cancel(true);
                    }
                });
        return result;
    }

    @Override
    public <T> CompletableFuture<DetailedResponse<T>> executeDetailedAsync(
            final ClientRequest request, final Class<T> responseType) {
        final AsyncOperation<T> operation = new AsyncOperation<>();
        this.lifecycle.register(operation);
        operation.whenComplete((ignored, failure) -> this.lifecycle.deregister(operation));
        this.dispatchAsync(request, responseType, this.support.newState(request), operation);
        return operation;
    }

    private <T> void dispatchAsync(
            final ClientRequest request,
            final Class<T> responseType,
            final ExecutionState state,
            final AsyncOperation<T> operation) {

        if (operation.isDone()) return;

        try {
            this.support.ensureOpen();
            this.support.ensureDeadline(state);
            state.startAttempt();
            final CompletableFuture<TransportResponse> current =
                    this.transport.executeAsync(this.support.transportRequest(request, state));
            operation.current(current);
            current.whenCompleteAsync(
                    (response, failure) -> {
                        if (operation.isDone()) {
                            current.cancel(true);
                            return;
                        }
                        if (failure != null) {
                            this.handleAsyncFailure(
                                    request, responseType, state, operation, unwrap(failure));
                        } else {
                            this.handleAsyncResponse(
                                    request, responseType, state, operation, response);
                        }
                    },
                    this.executor);
        } catch (final SendByteException exception) {
            operation.completeExceptionally(exception);
        } catch (final RuntimeException exception) {
            operation.completeExceptionally(
                    support.failure(
                            request,
                            new ConnectionException(
                                    "Could not reach the SendByte API",
                                    exception,
                                    this.support.diagnostics(
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
                        this.executor.schedule(
                                () -> this.dispatchAsync(request, responseType, state, operation),
                                retry.delay().toNanos(),
                                TimeUnit.NANOSECONDS));
                return;
            }
            operation.complete(this.support.success(request, response, responseType, state));
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
            final RetryAttempt retry = this.support.retryForFailure(request, state, true);
            if (retry.retry()) {
                operation.scheduled(
                        this.executor.schedule(
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
                                    this.support.diagnostics(
                                            request, state, null, null, category, false))
                            : new ConnectionException(
                                    "Could not reach the SendByte API",
                                    failure,
                                    this.support.diagnostics(
                                            request, state, null, null, category, false));
            operation.completeExceptionally(
                    support.failure(request, exception, state, null, category, false));
        } catch (final RuntimeException exception) {
            operation.completeExceptionally(exception);
        }
    }

    private static Throwable unwrap(final Throwable failure) {
        Throwable current = failure;
        while ((current instanceof CompletionException || current instanceof ExecutionException)
                && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }
}

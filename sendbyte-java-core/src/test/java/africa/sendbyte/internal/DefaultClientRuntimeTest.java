package africa.sendbyte.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import africa.sendbyte.DetailedResponse;
import africa.sendbyte.FailureCategory;
import africa.sendbyte.RequestDiagnostics;
import africa.sendbyte.RequestObserver;
import africa.sendbyte.RetryPolicy;
import africa.sendbyte.exception.ApiException;
import africa.sendbyte.exception.AuthenticationException;
import africa.sendbyte.exception.AuthorizationException;
import africa.sendbyte.exception.ConflictException;
import africa.sendbyte.exception.ConnectionException;
import africa.sendbyte.exception.NotFoundException;
import africa.sendbyte.exception.RateLimitException;
import africa.sendbyte.exception.ResponseTooLargeException;
import africa.sendbyte.exception.SendByteTimeoutException;
import africa.sendbyte.exception.SerializationException;
import africa.sendbyte.exception.ValidationException;
import africa.sendbyte.spi.ClientRequest;
import africa.sendbyte.testing.FakeHttpTransport;
import africa.sendbyte.transport.HttpTransport;
import africa.sendbyte.transport.TransportRequest;
import africa.sendbyte.transport.TransportResponse;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

final class DefaultClientRuntimeTest {
    @Test
    void retriesSafeOperationsWithExactBackoffAndDiagnostics() {
        final FakeHttpTransport transport =
                new FakeHttpTransport()
                        .enqueue(
                                response(
                                        503,
                                        "{\"error\":{\"code\":\"service_unavailable\",\"message\":\"try later\",\"docs_url\":\"https://docs.sendbyte.africa/errors/service_unavailable\"}}"))
                        .enqueue(response(200, "{\"name\":\"done\"}", "x-request-id", "req_done"));
        final TestExecutionControls controls = TestExecutionControls.fixed(Instant.EPOCH, 0.5d);
        final DefaultClientRuntime runtime = runtime(transport, controls);

        final DetailedResponse<WireValue> response =
                runtime.executeDetailed(
                        ClientRequest.builder("resource.get", "GET", "/v1/resource").build(),
                        WireValue.class);

        assertEquals("done", response.value().name);
        assertEquals(2, response.diagnostics().attemptCount());
        assertEquals(Duration.ofMillis(100), response.diagnostics().retryDelay());
        assertEquals("req_done", response.metadata().requestId());
        assertEquals(2, transport.requests().size());
        assertEquals(Duration.ofSeconds(30), transport.requests().get(0).timeout());
        assertEquals(Duration.ofMillis(29_900), transport.requests().get(1).timeout());
    }

    @Test
    void respectsIntegerAndHttpDateRetryAfter() {
        final Instant start = Instant.parse("2026-09-16T10:00:00Z");
        final FakeHttpTransport transport =
                new FakeHttpTransport()
                        .enqueue(response(429, error("rate_limit_exceeded"), "Retry-After", "3"))
                        .enqueue(
                                response(
                                        429,
                                        error("rate_limit_exceeded"),
                                        "Retry-After",
                                        "Wed, 16 Sep 2026 10:00:07 GMT"))
                        .enqueue(response(200, "{\"name\":\"done\"}"));
        final TestExecutionControls controls = TestExecutionControls.fixed(start, 0.5d);
        final DefaultClientRuntime runtime =
                runtime(
                        transport,
                        controls,
                        RetryPolicy.builder()
                                .maxAttempts(3)
                                .initialBackoff(Duration.ofMillis(10))
                                .maxBackoff(Duration.ofSeconds(10))
                                .jitterRatio(0.0d)
                                .build(),
                        RequestObserver.NONE,
                        1024);

        final DetailedResponse<WireValue> response =
                runtime.executeDetailed(
                        ClientRequest.builder("email.send", "POST", "/v1/emails")
                                .idempotencyProtected(true)
                                .body(Collections.singletonMap("idempotency_key", "idem_123"))
                                .build(),
                        WireValue.class);

        assertEquals("done", response.value().name);
        assertEquals(3, response.diagnostics().attemptCount());
        assertEquals(Duration.ofSeconds(7), response.diagnostics().retryDelay());
    }

    @Test
    void doesNotRetryUnsafeOperationWithoutIdempotencyProtection() {
        final FakeHttpTransport transport =
                new FakeHttpTransport().enqueue(response(503, error("service_unavailable")));
        final DefaultClientRuntime runtime =
                runtime(
                        transport,
                        TestExecutionControls.fixed(Instant.EPOCH, 0.5d),
                        RetryPolicy.builder().maxAttempts(3).jitterRatio(0.0d).build(),
                        RequestObserver.NONE,
                        1024);

        final ApiException failure =
                assertThrows(
                        ApiException.class,
                        () ->
                                runtime.execute(
                                        ClientRequest.builder("email.send", "POST", "/v1/emails")
                                                .body(Collections.singletonMap("subject", "Hi"))
                                                .build(),
                                        WireValue.class));

        assertEquals(503, failure.statusCode());
        assertEquals(1, failure.diagnostics().attemptCount());
        assertEquals(1, transport.requests().size());
    }

    @Test
    void totalDeadlineStopsRetryDelays() {
        final FakeHttpTransport transport =
                new FakeHttpTransport()
                        .enqueue(response(503, error("service_unavailable")))
                        .enqueue(response(503, error("service_unavailable")));
        final DefaultClientRuntime runtime =
                runtime(
                        transport,
                        TestExecutionControls.fixed(Instant.EPOCH, 0.5d),
                        RetryPolicy.builder()
                                .maxAttempts(3)
                                .initialBackoff(Duration.ofMillis(20))
                                .maxBackoff(Duration.ofMillis(20))
                                .jitterRatio(0.0d)
                                .build(),
                        RequestObserver.NONE,
                        1024);

        final SendByteTimeoutException failure =
                assertThrows(
                        SendByteTimeoutException.class,
                        () ->
                                runtime.execute(
                                        ClientRequest.builder("resource.get", "GET", "/v1/resource")
                                                .options(
                                                        africa.sendbyte.RequestOptions.builder()
                                                                .timeout(Duration.ofMillis(10))
                                                                .build())
                                                .build(),
                                        WireValue.class));

        assertEquals(1, failure.diagnostics().attemptCount());
        assertEquals(Duration.ZERO, failure.diagnostics().retryDelay());
    }

    @Test
    void requestOptionsCanSuppressSdkRetriesWhenApplicationsRetryOutsideTheSdk() {
        final FakeHttpTransport transport =
                new FakeHttpTransport()
                        .enqueue(response(503, error("service_unavailable")))
                        .enqueue(response(200, "{\"name\":\"should-not-run\"}"));
        final DefaultClientRuntime runtime =
                runtime(
                        transport,
                        TestExecutionControls.fixed(Instant.EPOCH, 0.5d),
                        RetryPolicy.builder().maxAttempts(3).jitterRatio(0.0d).build(),
                        RequestObserver.NONE,
                        1024);

        final ApiException failure =
                assertThrows(
                        ApiException.class,
                        () ->
                                runtime.execute(
                                        ClientRequest.builder("resource.get", "GET", "/v1/resource")
                                                .options(
                                                        africa.sendbyte.RequestOptions.builder()
                                                                .maxAttempts(1)
                                                                .build())
                                                .build(),
                                        WireValue.class));

        assertEquals(1, failure.diagnostics().attemptCount());
        assertEquals(1, transport.requests().size());
    }

    @Test
    void distinguishesConnectionTimeoutAndSerializationFailures() {
        final DefaultClientRuntime connectionRuntime =
                runtime(
                        new FakeHttpTransport().enqueueFailure(new IOException("network down")),
                        TestExecutionControls.fixed(Instant.EPOCH, 0.5d),
                        RetryPolicy.NONE,
                        RequestObserver.NONE,
                        1024);
        final ConnectionException connection =
                assertThrows(
                        ConnectionException.class,
                        () ->
                                connectionRuntime.execute(
                                        ClientRequest.builder("resource.get", "GET", "/v1/resource")
                                                .build(),
                                        WireValue.class));
        assertEquals(FailureCategory.CONNECTION, connection.diagnostics().failureCategory());

        final DefaultClientRuntime serializationRuntime =
                runtime(
                        new FakeHttpTransport().enqueue(response(200, "{not-json")),
                        TestExecutionControls.fixed(Instant.EPOCH, 0.5d),
                        RetryPolicy.NONE,
                        RequestObserver.NONE,
                        1024);
        final SerializationException serialization =
                assertThrows(
                        SerializationException.class,
                        () ->
                                serializationRuntime.execute(
                                        ClientRequest.builder("resource.get", "GET", "/v1/resource")
                                                .build(),
                                        WireValue.class));
        assertEquals(FailureCategory.SERIALIZATION, serialization.diagnostics().failureCategory());

        final DefaultClientRuntime timeoutRuntime =
                runtime(
                        new FakeHttpTransport()
                                .enqueue(response(503, error("service_unavailable"))),
                        TestExecutionControls.fixed(Instant.EPOCH, 0.5d),
                        RetryPolicy.builder()
                                .maxAttempts(2)
                                .initialBackoff(Duration.ofMillis(20))
                                .maxBackoff(Duration.ofMillis(20))
                                .jitterRatio(0.0d)
                                .build(),
                        RequestObserver.NONE,
                        1024);
        final SendByteTimeoutException timeout =
                assertThrows(
                        SendByteTimeoutException.class,
                        () ->
                                timeoutRuntime.execute(
                                        ClientRequest.builder("resource.get", "GET", "/v1/resource")
                                                .options(
                                                        africa.sendbyte.RequestOptions.builder()
                                                                .timeout(Duration.ofMillis(1))
                                                                .build())
                                                .build(),
                                        WireValue.class));
        assertEquals(FailureCategory.TIMEOUT, timeout.diagnostics().failureCategory());
    }

    @Test
    void syncAndAsyncProduceEquivalentValuesAndDiagnostics() throws Exception {
        final TestExecutionControls syncControls = TestExecutionControls.fixed(Instant.EPOCH, 0.5d);
        final DefaultClientRuntime syncRuntime =
                runtime(
                        new FakeHttpTransport().enqueue(response(200, "{\"name\":\"sync\"}")),
                        syncControls);
        final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        final DefaultClientRuntime asyncRuntime =
                runtime(
                        new FakeHttpTransport().enqueue(response(200, "{\"name\":\"sync\"}")),
                        TestExecutionControls.fixed(Instant.EPOCH, 0.5d),
                        RetryPolicy.NONE,
                        RequestObserver.NONE,
                        1024,
                        executor);

        try {
            final DetailedResponse<WireValue> sync =
                    syncRuntime.executeDetailed(
                            ClientRequest.builder("resource.get", "GET", "/v1/resource").build(),
                            WireValue.class);
            final DetailedResponse<WireValue> async =
                    asyncRuntime
                            .executeDetailedAsync(
                                    ClientRequest.builder("resource.get", "GET", "/v1/resource")
                                            .build(),
                                    WireValue.class)
                            .get(5, TimeUnit.SECONDS);

            assertEquals(sync.value().name, async.value().name);
            assertEquals(sync.diagnostics().attemptCount(), async.diagnostics().attemptCount());
            assertEquals(
                    sync.diagnostics().failureCategory(), async.diagnostics().failureCategory());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void mapsDocumentedErrorsToTypedOrGenericExceptions() {
        assertInstanceOf(ValidationException.class, apiException("validation_error", 422));
        assertInstanceOf(AuthenticationException.class, apiException("authentication_error", 401));
        assertInstanceOf(AuthorizationException.class, apiException("authorization_error", 403));
        assertInstanceOf(NotFoundException.class, apiException("not_found", 404));
        assertInstanceOf(ConflictException.class, apiException("idempotency_conflict", 409));
        assertInstanceOf(RateLimitException.class, apiException("rate_limit_exceeded", 429));
        assertEquals(ApiException.class, apiException("domain_not_verified", 400).getClass());
    }

    @Test
    void asyncErrorsMatchSyncTypedMapping() throws Exception {
        final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        final DefaultClientRuntime runtime =
                runtime(
                        new FakeHttpTransport().enqueue(response(422, error("validation_error"))),
                        TestExecutionControls.fixed(Instant.EPOCH, 0.5d),
                        RetryPolicy.NONE,
                        RequestObserver.NONE,
                        1024,
                        executor);

        try {
            final Exception failure =
                    assertThrows(
                            Exception.class,
                            () ->
                                    runtime.executeAsync(
                                                    ClientRequest.builder(
                                                                    "resource.get",
                                                                    "GET",
                                                                    "/v1/resource")
                                                            .build(),
                                                    WireValue.class)
                                            .get(5, TimeUnit.SECONDS));
            assertInstanceOf(ValidationException.class, failure.getCause());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void cancellationDuringIoDuringBackoffAndAfterCompletionIsDeterministic() throws Exception {
        final ManualScheduledExecutor duringIoExecutor = new ManualScheduledExecutor();
        final PendingTransport pendingTransport = new PendingTransport();
        final DefaultClientRuntime duringIoRuntime =
                runtime(
                        pendingTransport,
                        TestExecutionControls.fixed(Instant.EPOCH, 0.5d),
                        RetryPolicy.NONE,
                        RequestObserver.NONE,
                        1024,
                        duringIoExecutor);
        final CompletableFuture<DetailedResponse<WireValue>> duringIo =
                duringIoRuntime.executeDetailedAsync(
                        ClientRequest.builder("resource.get", "GET", "/v1/resource").build(),
                        WireValue.class);

        assertTrue(duringIo.cancel(true));
        assertTrue(pendingTransport.pending.isCancelled());

        final ManualScheduledExecutor backoffExecutor = new ManualScheduledExecutor();
        final FakeHttpTransport backoffTransport =
                new FakeHttpTransport()
                        .enqueue(response(503, error("service_unavailable")))
                        .enqueue(response(200, "{\"name\":\"should-not-run\"}"));
        final DefaultClientRuntime backoffRuntime =
                runtime(
                        backoffTransport,
                        TestExecutionControls.fixed(Instant.EPOCH, 0.5d),
                        RetryPolicy.builder()
                                .maxAttempts(2)
                                .initialBackoff(Duration.ofSeconds(5))
                                .maxBackoff(Duration.ofSeconds(5))
                                .jitterRatio(0.0d)
                                .build(),
                        RequestObserver.NONE,
                        1024,
                        backoffExecutor);
        final CompletableFuture<DetailedResponse<WireValue>> duringBackoff =
                backoffRuntime.executeDetailedAsync(
                        ClientRequest.builder("resource.get", "GET", "/v1/resource").build(),
                        WireValue.class);
        backoffExecutor.runNext();

        assertTrue(duringBackoff.cancel(true));
        backoffExecutor.runAll();
        assertEquals(1, backoffTransport.requests().size());

        final ManualScheduledExecutor afterCompletionExecutor = new ManualScheduledExecutor();
        final DefaultClientRuntime afterCompletionRuntime =
                runtime(
                        new FakeHttpTransport().enqueue(response(200, "{\"name\":\"done\"}")),
                        TestExecutionControls.fixed(Instant.EPOCH, 0.5d),
                        RetryPolicy.NONE,
                        RequestObserver.NONE,
                        1024,
                        afterCompletionExecutor);
        final CompletableFuture<DetailedResponse<WireValue>> afterCompletion =
                afterCompletionRuntime.executeDetailedAsync(
                        ClientRequest.builder("resource.get", "GET", "/v1/resource").build(),
                        WireValue.class);
        afterCompletionExecutor.runAll();

        assertEquals("done", afterCompletion.get(5, TimeUnit.SECONDS).value().name);
        assertFalse(afterCompletion.cancel(true));
    }

    @Test
    void detailedSuccessExposesHeadersAndObserverFailuresAreContained() {
        final AtomicReference<RequestDiagnostics> observed = new AtomicReference<>();
        final DefaultClientRuntime runtime =
                runtime(
                        new FakeHttpTransport()
                                .enqueue(
                                        response(
                                                200,
                                                "{\"name\":\"ok\"}",
                                                "X-Request-Id",
                                                "req_123")),
                        TestExecutionControls.fixed(Instant.EPOCH, 0.5d),
                        RetryPolicy.NONE,
                        diagnostics -> {
                            observed.set(diagnostics);
                            throw new IllegalStateException("observer failed");
                        },
                        1024);

        final DetailedResponse<WireValue> response =
                runtime.executeDetailed(
                        ClientRequest.builder("resource.get", "GET", "/v1/resource").build(),
                        WireValue.class);

        assertEquals("ok", response.value().name);
        assertEquals("req_123", response.metadata().requestId());
        assertEquals("req_123", observed.get().requestId());
    }

    @Test
    void responseBodyLimitIsEnforcedBeforeDecoding() {
        final DefaultClientRuntime runtime =
                runtime(
                        new FakeHttpTransport().enqueue(response(200, "{\"name\":\"too-large\"}")),
                        TestExecutionControls.fixed(Instant.EPOCH, 0.5d),
                        RetryPolicy.NONE,
                        RequestObserver.NONE,
                        4);

        final ResponseTooLargeException failure =
                assertThrows(
                        ResponseTooLargeException.class,
                        () ->
                                runtime.execute(
                                        ClientRequest.builder("resource.get", "GET", "/v1/resource")
                                                .build(),
                                        WireValue.class));

        assertEquals(FailureCategory.RESPONSE_TOO_LARGE, failure.diagnostics().failureCategory());
    }

    @Test
    void sanitizesSensitiveErrorMessages() {
        final DefaultClientRuntime runtime =
                runtime(
                        new FakeHttpTransport()
                                .enqueue(
                                        response(
                                                422,
                                                "{\"error\":{\"code\":\"validation_error\",\"message\":\"bad sk_redact_secret Authorization: Bearer token to user@example.com +234 800 123 4567 https://example.com/customer/abc 123456789012345678901234567890\",\"docs_url\":\"https://docs.sendbyte.africa/errors/validation_error\"}}")),
                        TestExecutionControls.fixed(Instant.EPOCH, 0.5d));

        final ValidationException failure =
                assertThrows(
                        ValidationException.class,
                        () ->
                                runtime.execute(
                                        ClientRequest.builder("email.send", "POST", "/v1/emails")
                                                .body(
                                                        Collections.singletonMap(
                                                                "to", "user@example.com"))
                                                .build(),
                                        WireValue.class));

        assertFalse(failure.getMessage().contains("sk_redact_secret"));
        assertFalse(failure.getMessage().contains("user@example.com"));
        assertFalse(failure.getMessage().contains("+234"));
        assertFalse(failure.getMessage().contains("https://example.com"));
        assertFalse(failure.getMessage().contains("123456789012345678901234567890"));
    }

    @Test
    void concurrentClientsDoNotLeakCredentials() throws Exception {
        final FakeHttpTransport first = new FakeHttpTransport().enqueue(response(204, ""));
        final FakeHttpTransport second = new FakeHttpTransport().enqueue(response(204, ""));
        final DefaultClientRuntime firstRuntime = runtime(first, "sk_first");
        final DefaultClientRuntime secondRuntime = runtime(second, "sk_second");

        final Thread firstThread =
                new Thread(
                        () ->
                                firstRuntime.execute(
                                        ClientRequest.builder("a.get", "GET", "/v1/a").build(),
                                        Void.class));
        final Thread secondThread =
                new Thread(
                        () ->
                                secondRuntime.execute(
                                        ClientRequest.builder("b.get", "GET", "/v1/b").build(),
                                        Void.class));

        firstThread.start();
        secondThread.start();
        firstThread.join();
        secondThread.join();

        assertEquals(
                Collections.singletonList("Bearer sk_first"),
                first.requests().get(0).headers().get("authorization"));
        assertEquals(
                Collections.singletonList("Bearer sk_second"),
                second.requests().get(0).headers().get("authorization"));
    }

    private static ApiException apiException(final String code, final int status) {
        final DefaultClientRuntime runtime =
                runtime(
                        new FakeHttpTransport().enqueue(response(status, error(code))),
                        TestExecutionControls.fixed(Instant.EPOCH, 0.5d),
                        RetryPolicy.NONE,
                        RequestObserver.NONE,
                        1024);
        return assertThrows(
                ApiException.class,
                () ->
                        runtime.execute(
                                ClientRequest.builder("resource.get", "GET", "/v1/resource")
                                        .build(),
                                WireValue.class));
    }

    private static DefaultClientRuntime runtime(
            final HttpTransport transport, final TestExecutionControls controls) {
        return runtime(transport, controls, RetryPolicy.DEFAULT, RequestObserver.NONE, 1024);
    }

    private static DefaultClientRuntime runtime(
            final HttpTransport transport,
            final TestExecutionControls controls,
            final RetryPolicy retryPolicy,
            final RequestObserver observer,
            final long maxResponseBodyBytes) {
        return runtime(
                transport,
                controls,
                retryPolicy,
                observer,
                maxResponseBodyBytes,
                Executors.newSingleThreadScheduledExecutor());
    }

    private static DefaultClientRuntime runtime(
            final HttpTransport transport,
            final TestExecutionControls controls,
            final RetryPolicy retryPolicy,
            final RequestObserver observer,
            final long maxResponseBodyBytes,
            final ScheduledExecutorService executor) {
        return new DefaultClientRuntime(
                URI.create("https://api.test"),
                () -> "sk_test_example",
                transport,
                Duration.ofSeconds(30),
                "sendbyte-test",
                retryPolicy,
                observer,
                maxResponseBodyBytes,
                executor,
                controls.clock(),
                controls.ticker(),
                controls.sleeper(),
                controls.random(),
                new java.util.concurrent.atomic.AtomicBoolean());
    }

    private static DefaultClientRuntime runtime(
            final FakeHttpTransport transport, final String key) {
        final TestExecutionControls controls = TestExecutionControls.fixed(Instant.EPOCH, 0.5d);
        return new DefaultClientRuntime(
                URI.create("https://api.test"),
                () -> key,
                transport,
                Duration.ofSeconds(30),
                "sendbyte-test",
                RetryPolicy.NONE,
                RequestObserver.NONE,
                1024,
                Executors.newSingleThreadScheduledExecutor(),
                controls.clock(),
                controls.ticker(),
                controls.sleeper(),
                controls.random(),
                new java.util.concurrent.atomic.AtomicBoolean());
    }

    private static TransportResponse response(final int status, final String body) {
        return response(status, body, "content-type", "application/json");
    }

    private static TransportResponse response(
            final int status,
            final String body,
            final String headerName,
            final String headerValue) {
        return new TransportResponse(
                status,
                Collections.singletonMap(headerName, Collections.singletonList(headerValue)),
                body.getBytes(StandardCharsets.UTF_8));
    }

    private static String error(final String code) {
        return "{\"error\":{\"code\":\""
                + code
                + "\",\"message\":\""
                + code
                + "\",\"docs_url\":\"https://docs.sendbyte.africa/errors/"
                + code
                + "\"}}";
    }

    private static final class PendingTransport implements HttpTransport {
        private final CompletableFuture<TransportResponse> pending = new CompletableFuture<>();

        @Override
        public TransportResponse execute(final TransportRequest request) throws IOException {
            throw new IOException("not used");
        }

        @Override
        public CompletableFuture<TransportResponse> executeAsync(final TransportRequest request) {
            return this.pending;
        }
    }

    private static final class ManualScheduledExecutor implements ScheduledExecutorService {
        private final ArrayDeque<Runnable> tasks = new ArrayDeque<>();

        private void runNext() {
            this.tasks.remove().run();
        }

        private void runAll() {
            while (!this.tasks.isEmpty()) {
                this.runNext();
            }
        }

        @Override
        public void execute(final Runnable command) {
            this.tasks.add(command);
        }

        @Override
        public ScheduledFuture<?> schedule(
                final Runnable command, final long delay, final TimeUnit unit) {
            final ManualScheduledFuture<?> future = new ManualScheduledFuture<>(command, null);
            this.tasks.add(future);
            return future;
        }

        @Override
        public <V> ScheduledFuture<V> schedule(
                final Callable<V> callable, final long delay, final TimeUnit unit) {
            final ManualScheduledFuture<V> future = new ManualScheduledFuture<>(callable);
            this.tasks.add(future);
            return future;
        }

        @Override
        public void shutdown() {}

        @Override
        public List<Runnable> shutdownNow() {
            final List<Runnable> remaining = new ArrayList<>(this.tasks);
            this.tasks.clear();
            return remaining;
        }

        @Override
        public boolean isShutdown() {
            return false;
        }

        @Override
        public boolean isTerminated() {
            return false;
        }

        @Override
        public boolean awaitTermination(final long timeout, final TimeUnit unit) {
            return true;
        }

        @Override
        public <T> Future<T> submit(final Callable<T> task) {
            final ManualScheduledFuture<T> future = new ManualScheduledFuture<>(task);
            this.tasks.add(future);
            return future;
        }

        @Override
        public <T> Future<T> submit(final Runnable task, final T result) {
            final ManualScheduledFuture<T> future = new ManualScheduledFuture<>(task, result);
            this.tasks.add(future);
            return future;
        }

        @Override
        public Future<?> submit(final Runnable task) {
            return this.submit(task, null);
        }

        @Override
        public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> List<Future<T>> invokeAll(
                final Collection<? extends Callable<T>> tasks,
                final long timeout,
                final TimeUnit unit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T invokeAny(final Collection<? extends Callable<T>> tasks) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T invokeAny(
                final Collection<? extends Callable<T>> tasks,
                final long timeout,
                final TimeUnit unit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(
                final Runnable command,
                final long initialDelay,
                final long period,
                final TimeUnit unit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(
                final Runnable command,
                final long initialDelay,
                final long delay,
                final TimeUnit unit) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class ManualScheduledFuture<V> implements RunnableScheduledFuture<V> {
        private final Callable<V> callable;
        private boolean canceled;
        private boolean done;
        private V value;
        private Exception failure;

        private ManualScheduledFuture(final Runnable runnable, final V value) {
            this.callable =
                    () -> {
                        runnable.run();
                        return value;
                    };
        }

        private ManualScheduledFuture(final Callable<V> callable) {
            this.callable = callable;
        }

        @Override
        public void run() {
            if (this.canceled) {
                return;
            }
            try {
                this.value = this.callable.call();
            } catch (final Exception exception) {
                this.failure = exception;
            }
            this.done = true;
        }

        @Override
        public boolean isPeriodic() {
            return false;
        }

        @Override
        public long getDelay(final TimeUnit unit) {
            return 0L;
        }

        @Override
        public int compareTo(final Delayed other) {
            return 0;
        }

        @Override
        public boolean cancel(final boolean mayInterruptIfRunning) {
            if (this.done) {
                return false;
            }
            this.canceled = true;
            this.done = true;
            return true;
        }

        @Override
        public boolean isCancelled() {
            return this.canceled;
        }

        @Override
        public boolean isDone() {
            return this.done;
        }

        @Override
        public V get() throws ExecutionException {
            if (this.failure != null) {
                throw new ExecutionException(this.failure);
            }
            return this.value;
        }

        @Override
        public V get(final long timeout, final TimeUnit unit) throws ExecutionException {
            return this.get();
        }
    }

    public static final class WireValue {
        public String name;
    }
}

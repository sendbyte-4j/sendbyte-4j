package africa.sendbyte.testing;

import africa.sendbyte.transport.HttpTransport;
import africa.sendbyte.transport.TransportRequest;
import africa.sendbyte.transport.TransportResponse;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Internal test-support fake transport.
 *
 * <p>This type is provided for SDK module tests and consumer compatibility fixtures. It is not a
 * stable application API and may change between SDK versions.
 */
public final class FakeHttpTransport implements HttpTransport {
    private final Queue<Object> outcomes = new ArrayDeque<>();
    private final List<TransportRequest> requests = new ArrayList<>();
    private final AtomicInteger closeCount = new AtomicInteger();

    public FakeHttpTransport enqueue(final TransportResponse response) {
        this.outcomes.add(Objects.requireNonNull(response, "response"));
        return this;
    }

    public FakeHttpTransport enqueueFailure(final IOException failure) {
        this.outcomes.add(Objects.requireNonNull(failure, "failure"));
        return this;
    }

    public List<TransportRequest> requests() {
        return Collections.unmodifiableList(this.requests);
    }

    public int closeCount() {
        return this.closeCount.get();
    }

    @Override
    public TransportResponse execute(final TransportRequest request) throws IOException {
        this.requests.add(request);
        final Object outcome = this.outcomes.remove();
        if (outcome instanceof IOException) {
            throw (IOException) outcome;
        }
        return (TransportResponse) outcome;
    }

    @Override
    public CompletableFuture<TransportResponse> executeAsync(final TransportRequest request) {
        try {
            return CompletableFuture.completedFuture(this.execute(request));
        } catch (final IOException exception) {
            final CompletableFuture<TransportResponse> failed = new CompletableFuture<>();
            failed.completeExceptionally(exception);
            return failed;
        }
    }

    @Override
    public void close() {
        this.closeCount.incrementAndGet();
    }
}

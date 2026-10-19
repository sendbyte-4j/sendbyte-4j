package africa.sendbyte.internal;

import africa.sendbyte.DetailedResponse;
import africa.sendbyte.transport.TransportResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;

final class AsyncOperation<T> extends CompletableFuture<DetailedResponse<T>> {
    private volatile CompletableFuture<TransportResponse> current;
    private volatile ScheduledFuture<?> scheduled;

    void current(final CompletableFuture<TransportResponse> current) {
        this.current = current;
    }

    void scheduled(final ScheduledFuture<?> scheduled) {
        this.scheduled = scheduled;
    }

    /**
     * Cancels the in-flight transport work and any pending retry, then fails this operation. Used
     * when the client closes so that callers never wait on a future that cannot complete.
     */
    void abort(final Throwable failure) {
        final CompletableFuture<TransportResponse> inFlight = current;
        if (inFlight != null) {
            inFlight.cancel(true);
        }
        final ScheduledFuture<?> pendingRetry = scheduled;
        if (pendingRetry != null) {
            pendingRetry.cancel(false);
        }
        super.completeExceptionally(failure);
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        final CompletableFuture<TransportResponse> currentRequest = current;
        if (currentRequest != null) {
            currentRequest.cancel(mayInterruptIfRunning);
        }
        final ScheduledFuture<?> scheduledRetry = scheduled;
        if (scheduledRetry != null) {
            scheduledRetry.cancel(mayInterruptIfRunning);
        }
        return super.cancel(mayInterruptIfRunning);
    }
}

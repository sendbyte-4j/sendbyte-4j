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

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        final CompletableFuture<TransportResponse> currentRequest = this.current;
        if (currentRequest != null) {
            currentRequest.cancel(mayInterruptIfRunning);
        }
        final ScheduledFuture<?> scheduledRetry = this.scheduled;
        if (scheduledRetry != null) {
            scheduledRetry.cancel(mayInterruptIfRunning);
        }
        return super.cancel(mayInterruptIfRunning);
    }
}

package africa.sendbyte.internal;

import africa.sendbyte.spi.ClientRequest;
import java.time.Duration;
import java.util.function.Function;

final class ExecutionState {
    private final ClientRequest request;
    private final long startedAtNanos;
    private final long deadlineNanos;
    private int attemptCount;
    private Duration retryDelay = Duration.ZERO;
    private byte[] encodedBody;
    private boolean encodedBodyResolved;

    ExecutionState(
            final ClientRequest request, final Duration defaultTimeout, final Ticker ticker) {
        this.request = request;
        this.startedAtNanos = ticker.nanoTime();
        final Duration timeout =
                request.options().timeout() == null ? defaultTimeout : request.options().timeout();
        this.deadlineNanos = this.startedAtNanos + timeout.toNanos();
    }

    ClientRequest request() {
        return this.request;
    }

    /**
     * Returns the serialized request body, encoding it at most once per execution so that retries
     * reuse the bytes instead of running the codec again.
     *
     * <p>Attempts of one execution never overlap, so the cached value needs no synchronization.
     */
    byte[] encodedBody(final Function<ClientRequest, byte[]> encoder) {
        if (!this.encodedBodyResolved) {
            this.encodedBody = encoder.apply(this.request);
            this.encodedBodyResolved = true;
        }
        return this.encodedBody;
    }

    void startAttempt() {
        this.attemptCount++;
    }

    int attemptCount() {
        return this.attemptCount;
    }

    Duration retryDelay() {
        return this.retryDelay;
    }

    void addRetryDelay(final Duration delay) {
        this.retryDelay = this.retryDelay.plus(delay);
    }

    Duration remaining(final Ticker ticker) {
        return Duration.ofNanos(this.deadlineNanos - ticker.nanoTime());
    }

    Duration totalDuration(final Ticker ticker) {
        return Duration.ofNanos(Math.max(0L, ticker.nanoTime() - this.startedAtNanos));
    }
}

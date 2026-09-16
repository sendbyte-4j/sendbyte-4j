package africa.sendbyte.internal;

import africa.sendbyte.spi.ClientRequest;
import java.time.Duration;

final class ExecutionState {
    private final ClientRequest request;
    private final long startedAtNanos;
    private final long deadlineNanos;
    private int attemptCount;
    private Duration retryDelay = Duration.ZERO;

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

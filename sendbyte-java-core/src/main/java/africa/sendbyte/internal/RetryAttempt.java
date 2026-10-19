package africa.sendbyte.internal;

import java.time.Duration;

final class RetryAttempt {
    private final boolean retry;
    private final Duration delay;

    private RetryAttempt(final boolean retry, final Duration delay) {
        this.retry = retry;
        this.delay = delay;
    }

    static RetryAttempt retry(final Duration delay) {
        return new RetryAttempt(true, delay);
    }

    static RetryAttempt none() {
        return new RetryAttempt(false, Duration.ZERO);
    }

    boolean retry() {
        return retry;
    }

    Duration delay() {
        return delay;
    }
}

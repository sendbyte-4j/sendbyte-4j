package africa.sendbyte.internal;

import africa.sendbyte.RetryPolicy;
import africa.sendbyte.spi.ClientRequest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.function.DoubleSupplier;

final class RetryPlanner {
    private static final Duration MAX_NANOS = Duration.ofNanos(Long.MAX_VALUE);

    private final RetryPolicy retryPolicy;
    private final Clock clock;
    private final DoubleSupplier random;

    RetryPlanner(final RetryPolicy retryPolicy, final Clock clock, final DoubleSupplier random) {
        this.retryPolicy = retryPolicy;
        this.clock = clock;
        this.random = random;
    }

    boolean canRetryResponse(final ClientRequest request, final int statusCode) {
        return this.isRetryableStatus(statusCode) && this.canRetryOperation(request);
    }

    boolean canRetryFailure(final ClientRequest request, final boolean retryableFailure) {
        return retryableFailure && this.canRetryOperation(request);
    }

    boolean canRetryAttempt(
            final ClientRequest request,
            final ExecutionState state,
            final Duration delay,
            final Ticker ticker) {
        return state.attemptCount() < this.maxAttempts(request)
                && state.remaining(ticker).compareTo(delay) > 0;
    }

    boolean retryBlockedByDeadline(
            final ClientRequest request,
            final ExecutionState state,
            final Duration delay,
            final Ticker ticker) {
        return state.attemptCount() < this.maxAttempts(request)
                && state.remaining(ticker).compareTo(delay) <= 0;
    }

    Duration delay(final String retryAfterHeader, final int attemptCount) {
        final Duration retryAfter = this.retryAfter(retryAfterHeader);
        if (retryAfter != null) {
            return RetryPlanner.cap(retryAfter, retryPolicy.maxBackoff());
        }

        final long exponent = 1L << Math.min(30, attemptCount - 1);
        final Duration capped =
                RetryPlanner.cappedBackoff(
                        retryPolicy.initialBackoff(), retryPolicy.maxBackoff(), exponent);
        if (retryPolicy.jitterRatio() == 0.0d || capped.isZero()) {
            return capped;
        }

        final double jitter =
                1.0d + ((random.getAsDouble() * 2.0d) - 1.0d) * retryPolicy.jitterRatio();
        return RetryPlanner.multiply(capped, jitter);
    }

    Duration retryAfter(final String value) {
        if (value == null) {
            return null;
        }
        try {
            return Duration.ofSeconds(Long.parseLong(value.trim()));
        } catch (final NumberFormatException ignored) {
            try {
                final Instant retryAt =
                        ZonedDateTime.parse(value.trim(), DateTimeFormatter.RFC_1123_DATE_TIME)
                                .toInstant();
                final Duration delay = Duration.between(clock.instant(), retryAt);
                return delay.isNegative() ? Duration.ZERO : delay;
            } catch (final DateTimeParseException ignoredAgain) {
                return null;
            }
        }
    }

    private int maxAttempts(final ClientRequest request) {
        return request.options().maxAttempts() == null
                ? retryPolicy.maxAttempts()
                : request.options().maxAttempts();
    }

    private boolean canRetryOperation(final ClientRequest request) {
        final String method = request.method().toUpperCase(Locale.ROOT);
        return "GET".equals(method)
                || "HEAD".equals(method)
                || "OPTIONS".equals(method)
                || "DELETE".equals(method)
                || request.idempotencyProtected();
    }

    private boolean isRetryableStatus(final int statusCode) {
        return statusCode == 429
                || statusCode == 500
                || statusCode == 502
                || statusCode == 503
                || statusCode == 504;
    }

    private static Duration cap(final Duration value, final Duration maximum) {
        return value.compareTo(maximum) > 0 ? maximum : value;
    }

    /**
     * Returns {@code initialBackoff * exponent} limited to {@code maximum}.
     *
     * <p>The factor is bounded by the ratio to the cap before multiplying, because the product can
     * leave the range of {@link Duration} nanoseconds: doubling thirty times already exceeds it for
     * any backoff of a few seconds. A backoff or cap that cannot be expressed in nanoseconds at all
     * is reported as the cap, which is where the backoff saturates anyway.
     */
    private static Duration cappedBackoff(
            final Duration initialBackoff, final Duration maximum, final long exponent) {
        if (initialBackoff.isZero()) {
            return Duration.ZERO;
        }
        if (initialBackoff.compareTo(MAX_NANOS) >= 0 || maximum.compareTo(MAX_NANOS) >= 0) {
            return maximum;
        }

        final long initialNanos = initialBackoff.toNanos();
        final long maximumNanos = maximum.toNanos();
        if (exponent > maximumNanos / initialNanos) {
            return maximum;
        }
        return Duration.ofNanos(initialNanos * exponent);
    }

    private static Duration multiply(final Duration duration, final double factor) {
        return Duration.ofNanos(Math.max(0L, Math.round(duration.toNanos() * factor)));
    }
}

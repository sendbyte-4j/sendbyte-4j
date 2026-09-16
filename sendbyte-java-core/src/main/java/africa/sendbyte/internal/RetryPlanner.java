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
            return cap(retryAfter, this.retryPolicy.maxBackoff());
        }

        final long exponent = 1L << Math.min(30, attemptCount - 1);
        final Duration capped =
                cap(
                        multiply(this.retryPolicy.initialBackoff(), exponent),
                        this.retryPolicy.maxBackoff());
        if (this.retryPolicy.jitterRatio() == 0.0d || capped.isZero()) {
            return capped;
        }

        final double jitter =
                1.0d + ((this.random.getAsDouble() * 2.0d) - 1.0d) * this.retryPolicy.jitterRatio();
        return multiply(capped, jitter);
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
                final Duration delay = Duration.between(this.clock.instant(), retryAt);
                return delay.isNegative() ? Duration.ZERO : delay;
            } catch (final DateTimeParseException ignoredAgain) {
                return null;
            }
        }
    }

    private int maxAttempts(final ClientRequest request) {
        return request.options().maxAttempts() == null
                ? this.retryPolicy.maxAttempts()
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

    private static Duration multiply(final Duration duration, final long factor) {
        if (duration.isZero() || factor == 0L) {
            return Duration.ZERO;
        }
        return Duration.ofNanos(Math.multiplyExact(duration.toNanos(), factor));
    }

    private static Duration multiply(final Duration duration, final double factor) {
        return Duration.ofNanos(Math.max(0L, Math.round(duration.toNanos() * factor)));
    }
}

package africa.sendbyte;

import java.time.Duration;
import java.util.Objects;

/**
 * Immutable retry policy for conservative SDK retries.
 *
 * <p>Application, framework, proxy, and service-mesh retries multiply with SDK retries. Set
 * request-level max attempts to {@code 1} when another layer already owns retry behavior.
 */
public final class RetryPolicy {
    public static final RetryPolicy DEFAULT =
            RetryPolicy.builder()
                    .maxAttempts(3)
                    .initialBackoff(Duration.ofMillis(100))
                    .maxBackoff(Duration.ofSeconds(2))
                    .jitterRatio(0.2d)
                    .build();
    public static final RetryPolicy NONE = RetryPolicy.builder().maxAttempts(1).build();

    private final int maxAttempts;
    private final Duration initialBackoff;
    private final Duration maxBackoff;
    private final double jitterRatio;

    private RetryPolicy(final Builder builder) {
        this.maxAttempts = builder.maxAttempts;
        this.initialBackoff = builder.initialBackoff;
        this.maxBackoff = builder.maxBackoff;
        this.jitterRatio = builder.jitterRatio;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int maxAttempts() {
        return maxAttempts;
    }

    public Duration initialBackoff() {
        return initialBackoff;
    }

    public Duration maxBackoff() {
        return maxBackoff;
    }

    public double jitterRatio() {
        return jitterRatio;
    }

    public static final class Builder {
        private int maxAttempts = 3;
        private Duration initialBackoff = Duration.ofMillis(100);
        private Duration maxBackoff = Duration.ofSeconds(2);
        private double jitterRatio = 0.2d;

        private Builder() {}

        public Builder maxAttempts(final int maxAttempts) {
            if (maxAttempts < 1) {
                throw new IllegalArgumentException("maxAttempts must be positive");
            }
            this.maxAttempts = maxAttempts;
            return this;
        }

        public Builder initialBackoff(final Duration initialBackoff) {
            this.initialBackoff = Builder.positiveOrZero(initialBackoff, "initialBackoff");
            return this;
        }

        public Builder maxBackoff(final Duration maxBackoff) {
            this.maxBackoff = Builder.positiveOrZero(maxBackoff, "maxBackoff");
            return this;
        }

        public Builder jitterRatio(final double jitterRatio) {
            if (Double.isNaN(jitterRatio) || jitterRatio < 0.0d || jitterRatio > 1.0d) {
                throw new IllegalArgumentException("jitterRatio must be between 0.0 and 1.0");
            }
            this.jitterRatio = jitterRatio;
            return this;
        }

        public RetryPolicy build() {
            if (maxBackoff.compareTo(initialBackoff) < 0) {
                throw new IllegalArgumentException(
                        "maxBackoff must be greater than or equal to initialBackoff");
            }
            return new RetryPolicy(this);
        }

        private static Duration positiveOrZero(final Duration value, final String name) {
            Objects.requireNonNull(value, name);
            if (value.isNegative()) {
                throw new IllegalArgumentException(name + " must not be negative");
            }
            return value;
        }
    }
}

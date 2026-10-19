package africa.sendbyte;

import java.time.Duration;
import java.util.Objects;

/** Request-level overrides for the shared execution pipeline. */
public final class RequestOptions {
    public static final RequestOptions DEFAULT = RequestOptions.builder().build();

    private final Duration timeout;
    private final Integer maxAttempts;

    private RequestOptions(final Builder builder) {
        this.timeout = builder.timeout;
        this.maxAttempts = builder.maxAttempts;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Duration timeout() {
        return timeout;
    }

    public Integer maxAttempts() {
        return maxAttempts;
    }

    public static final class Builder {
        private Duration timeout;
        private Integer maxAttempts;

        private Builder() {}

        public Builder timeout(final Duration timeout) {
            this.timeout = Builder.positive(timeout, "timeout");
            return this;
        }

        public Builder maxAttempts(final int maxAttempts) {
            if (maxAttempts < 1) {
                throw new IllegalArgumentException("maxAttempts must be positive");
            }
            this.maxAttempts = maxAttempts;
            return this;
        }

        public RequestOptions build() {
            return new RequestOptions(this);
        }

        private static Duration positive(final Duration value, final String name) {
            Objects.requireNonNull(value, name);
            if (value.isZero() || value.isNegative()) {
                throw new IllegalArgumentException(name + " must be positive");
            }
            return value;
        }
    }
}

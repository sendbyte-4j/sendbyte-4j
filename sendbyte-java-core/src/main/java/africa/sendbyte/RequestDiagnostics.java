package africa.sendbyte;

import java.time.Duration;
import java.util.Objects;

/** Immutable, sanitized facts about one SDK operation. */
public final class RequestDiagnostics {
    private final String operation;
    private final String requestId;
    private final Integer statusCode;
    private final FailureCategory failureCategory;
    private final Duration totalDuration;
    private final int attemptCount;
    private final Duration retryDelay;
    private final boolean retryable;

    private RequestDiagnostics(final Builder builder) {
        this.operation = builder.operation;
        this.requestId = builder.requestId;
        this.statusCode = builder.statusCode;
        this.failureCategory = builder.failureCategory;
        this.totalDuration = builder.totalDuration;
        this.attemptCount = builder.attemptCount;
        this.retryDelay = builder.retryDelay;
        this.retryable = builder.retryable;
    }

    public static Builder builder(final String operation) {
        return new Builder(operation);
    }

    public String operation() {
        return operation;
    }

    public String requestId() {
        return requestId;
    }

    public Integer statusCode() {
        return statusCode;
    }

    public FailureCategory failureCategory() {
        return failureCategory;
    }

    public Duration totalDuration() {
        return totalDuration;
    }

    public int attemptCount() {
        return attemptCount;
    }

    public Duration retryDelay() {
        return retryDelay;
    }

    public boolean retryable() {
        return retryable;
    }

    @Override
    public String toString() {
        return "RequestDiagnostics{"
                + "operation='"
                + operation
                + '\''
                + ", requestId='"
                + requestId
                + '\''
                + ", statusCode="
                + statusCode
                + ", failureCategory="
                + failureCategory
                + ", totalDuration="
                + totalDuration
                + ", attemptCount="
                + attemptCount
                + ", retryDelay="
                + retryDelay
                + ", retryable="
                + retryable
                + '}';
    }

    public static final class Builder {
        private final String operation;
        private String requestId;
        private Integer statusCode;
        private FailureCategory failureCategory;
        private Duration totalDuration = Duration.ZERO;
        private int attemptCount = 1;
        private Duration retryDelay = Duration.ZERO;
        private boolean retryable;

        private Builder(final String operation) {
            this.operation = Builder.requireText(operation, "operation");
        }

        public Builder requestId(final String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder statusCode(final Integer statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder failureCategory(final FailureCategory failureCategory) {
            this.failureCategory = failureCategory;
            return this;
        }

        public Builder totalDuration(final Duration totalDuration) {
            this.totalDuration = Objects.requireNonNull(totalDuration, "totalDuration");
            return this;
        }

        public Builder attemptCount(final int attemptCount) {
            if (attemptCount < 1) {
                throw new IllegalArgumentException("attemptCount must be positive");
            }
            this.attemptCount = attemptCount;
            return this;
        }

        public Builder retryDelay(final Duration retryDelay) {
            this.retryDelay = Objects.requireNonNull(retryDelay, "retryDelay");
            return this;
        }

        public Builder retryable(final boolean retryable) {
            this.retryable = retryable;
            return this;
        }

        public RequestDiagnostics build() {
            return new RequestDiagnostics(this);
        }

        private static String requireText(final String value, final String name) {
            Objects.requireNonNull(value, name);
            if (value.trim().isEmpty()) {
                throw new IllegalArgumentException(name + " must not be blank");
            }
            return value;
        }
    }
}

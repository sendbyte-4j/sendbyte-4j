package africa.sendbyte.email.request;

import africa.sendbyte.email.EmailStatus;
import java.util.Objects;

/** Filters for listing emails. */
public final class ListEmailsRequest {
    private final Integer limit;
    private final String after;
    private final EmailStatus status;

    private ListEmailsRequest(final Builder builder) {
        this.limit = builder.limit;
        this.after = optionalNonBlank(builder.after, "after", 256);
        this.status = builder.status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Integer limit() {
        return this.limit;
    }

    public String after() {
        return this.after;
    }

    public EmailStatus status() {
        return this.status;
    }

    public static final class Builder {
        private Integer limit;
        private String after;
        private EmailStatus status;

        private Builder() {}

        public Builder limit(final int limit) {
            if (limit < 1 || limit > 100) {
                throw new IllegalArgumentException("limit must be between 1 and 100");
            }
            this.limit = limit;
            return this;
        }

        public Builder after(final String after) {
            this.after = after;
            return this;
        }

        public Builder status(final EmailStatus status) {
            this.status = status;
            return this;
        }

        public ListEmailsRequest build() {
            return new ListEmailsRequest(this);
        }
    }

    static String optionalNonBlank(final String value, final String name, final int maximum) {
        if (value == null) {
            return null;
        }
        Objects.requireNonNull(value, name);
        if (value.trim().isEmpty() || value.length() > maximum) {
            throw new IllegalArgumentException(
                    name + " must be non-blank and contain at most " + maximum + " characters");
        }
        return value;
    }
}

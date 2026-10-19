package africa.sendbyte.email.request;

import africa.sendbyte.internal.WireSupport;

/** Filters for listing suppressed recipient addresses. */
public final class ListSuppressionsRequest {
    private final String search;
    private final Integer limit;

    private ListSuppressionsRequest(final Builder builder) {
        this.search = WireSupport.optionalText(builder.search, "search", 320);
        this.limit = builder.limit;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String search() {
        return this.search;
    }

    public Integer limit() {
        return this.limit;
    }

    public static final class Builder {
        private String search;
        private Integer limit;

        private Builder() {}

        public Builder search(final String search) {
            this.search = search;
            return this;
        }

        public Builder limit(final int limit) {
            if (limit < 1 || limit > 200) {
                throw new IllegalArgumentException("limit must be between 1 and 200");
            }
            this.limit = limit;
            return this;
        }

        public ListSuppressionsRequest build() {
            return new ListSuppressionsRequest(this);
        }
    }
}

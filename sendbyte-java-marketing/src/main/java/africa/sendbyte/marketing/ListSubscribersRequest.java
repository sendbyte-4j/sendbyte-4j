package africa.sendbyte.marketing;

/** Filters for listing marketing subscribers. */
public final class ListSubscribersRequest {
    private final String query;
    private final SubscriberStatus status;
    private final Integer limit;
    private final Integer offset;

    private ListSubscribersRequest(final Builder builder) {
        this.query = WireSupport.optionalText(builder.query, "subscriber search query");
        if (builder.status == SubscriberStatus.UNKNOWN) {
            throw new IllegalArgumentException("subscriber status filter must be known");
        }
        if (builder.limit != null && (builder.limit < 1 || builder.limit > 100)) {
            throw new IllegalArgumentException("subscriber list limit must be between 1 and 100");
        }
        if (builder.offset != null && builder.offset < 0) {
            throw new IllegalArgumentException("subscriber list offset must be zero or positive");
        }
        this.status = builder.status;
        this.limit = builder.limit;
        this.offset = builder.offset;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String query() {
        return this.query;
    }

    public SubscriberStatus status() {
        return this.status;
    }

    public Integer limit() {
        return this.limit;
    }

    public Integer offset() {
        return this.offset;
    }

    /** Mutable builder for {@link ListSubscribersRequest}. */
    public static final class Builder {
        private String query;
        private SubscriberStatus status;
        private Integer limit;
        private Integer offset;

        private Builder() {}

        public Builder query(final String query) {
            this.query = query;
            return this;
        }

        public Builder status(final SubscriberStatus status) {
            this.status = status;
            return this;
        }

        public Builder limit(final Integer limit) {
            this.limit = limit;
            return this;
        }

        public Builder offset(final Integer offset) {
            this.offset = offset;
            return this;
        }

        public ListSubscribersRequest build() {
            return new ListSubscribersRequest(this);
        }
    }
}

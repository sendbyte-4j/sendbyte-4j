package africa.sendbyte.email.request;

import africa.sendbyte.email.EmailStatus;

/** Filters for exporting email history as CSV. */
public final class ExportEmailsRequest {
    private final EmailStatus status;
    private final String domainId;
    private final Boolean sandbox;
    private final Integer days;
    private final String search;

    private ExportEmailsRequest(final Builder builder) {
        this.status = builder.status;
        this.domainId = ListEmailsRequest.optionalNonBlank(builder.domainId, "domainId", 256);
        this.sandbox = builder.sandbox;
        this.days = builder.days;
        this.search = ListEmailsRequest.optionalNonBlank(builder.search, "search", 200);
    }

    public static Builder builder() {
        return new Builder();
    }

    public EmailStatus status() {
        return this.status;
    }

    public String domainId() {
        return this.domainId;
    }

    public Boolean sandbox() {
        return this.sandbox;
    }

    public Integer days() {
        return this.days;
    }

    public String search() {
        return this.search;
    }

    public static final class Builder {
        private EmailStatus status;
        private String domainId;
        private Boolean sandbox;
        private Integer days;
        private String search;

        private Builder() {}

        public Builder status(final EmailStatus status) {
            this.status = status;
            return this;
        }

        public Builder domainId(final String domainId) {
            this.domainId = domainId;
            return this;
        }

        public Builder sandbox(final Boolean sandbox) {
            this.sandbox = sandbox;
            return this;
        }

        public Builder days(final int days) {
            if (days < 1 || days > 3650) {
                throw new IllegalArgumentException("days must be between 1 and 3650");
            }
            this.days = days;
            return this;
        }

        public Builder search(final String search) {
            this.search = search;
            return this;
        }

        public ExportEmailsRequest build() {
            return new ExportEmailsRequest(this);
        }
    }
}

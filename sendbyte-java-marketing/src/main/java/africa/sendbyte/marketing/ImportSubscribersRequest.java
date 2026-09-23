package africa.sendbyte.marketing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Request used to import up to 1,000 subscribers into a marketing list. */
public final class ImportSubscribersRequest {
    private final List<SubscriberImport> subscribers;

    private ImportSubscribersRequest(final Builder builder) {
        if (builder.subscribers.isEmpty() || builder.subscribers.size() > 1000) {
            throw new IllegalArgumentException(
                    "subscriber import must contain between 1 and 1000 subscribers");
        }
        this.subscribers = Collections.unmodifiableList(new ArrayList<>(builder.subscribers));
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<SubscriberImport> subscribers() {
        return this.subscribers;
    }

    /** Mutable builder for {@link ImportSubscribersRequest}. */
    public static final class Builder {
        private final List<SubscriberImport> subscribers = new ArrayList<>();

        private Builder() {}

        public Builder subscriber(final SubscriberImport subscriber) {
            this.subscribers.add(Objects.requireNonNull(subscriber, "subscriber"));
            return this;
        }

        public Builder subscribers(final List<SubscriberImport> subscribers) {
            Objects.requireNonNull(subscribers, "subscribers");
            for (final SubscriberImport subscriber : subscribers) {
                this.subscriber(subscriber);
            }
            return this;
        }

        public ImportSubscribersRequest build() {
            return new ImportSubscribersRequest(this);
        }
    }
}

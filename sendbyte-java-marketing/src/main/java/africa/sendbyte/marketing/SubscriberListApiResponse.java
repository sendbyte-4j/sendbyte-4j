package africa.sendbyte.marketing;

import com.fasterxml.jackson.annotation.JsonProperty;

final class SubscriberListApiResponse {
    @JsonProperty("id")
    public String id;

    @JsonProperty("name")
    public String name;

    @JsonProperty("opt_in")
    public String optIn;

    @JsonProperty("counts")
    public CountsApiResponse counts;

    @JsonProperty("created_at")
    public String createdAt;

    SubscriberList toSubscriberList() {
        return new SubscriberList(
                this.id,
                this.name,
                this.optIn,
                this.counts == null ? null : this.counts.toSubscriberListCounts(),
                WireSupport.parseTimestamp(this.createdAt, "subscriber list created timestamp"));
    }

    static final class CountsApiResponse {
        @JsonProperty("subscribed")
        public int subscribed;

        @JsonProperty("unconfirmed")
        public int unconfirmed;

        @JsonProperty("unsubscribed")
        public int unsubscribed;

        @JsonProperty("bounced")
        public int bounced;

        @JsonProperty("complained")
        public int complained;

        SubscriberListCounts toSubscriberListCounts() {
            return new SubscriberListCounts(
                    this.subscribed,
                    this.unconfirmed,
                    this.unsubscribed,
                    this.bounced,
                    this.complained);
        }
    }
}

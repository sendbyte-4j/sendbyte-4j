package africa.sendbyte.marketing;

/** Membership counts for a marketing subscriber list. */
public final class SubscriberListCounts {
    private final int subscribed;
    private final int unconfirmed;
    private final int unsubscribed;
    private final int bounced;
    private final int complained;

    SubscriberListCounts(
            final int subscribed,
            final int unconfirmed,
            final int unsubscribed,
            final int bounced,
            final int complained) {
        this.subscribed = subscribed;
        this.unconfirmed = unconfirmed;
        this.unsubscribed = unsubscribed;
        this.bounced = bounced;
        this.complained = complained;
    }

    public int subscribed() {
        return this.subscribed;
    }

    public int unconfirmed() {
        return this.unconfirmed;
    }

    public int unsubscribed() {
        return this.unsubscribed;
    }

    public int bounced() {
        return this.bounced;
    }

    public int complained() {
        return this.complained;
    }
}

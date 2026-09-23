package africa.sendbyte.marketing;

/** Subscriber row rejected during an import. */
public final class RejectedSubscriber {
    private final String email;
    private final String reason;

    RejectedSubscriber(final String email, final String reason) {
        this.email = email;
        this.reason = reason;
    }

    public String email() {
        return this.email;
    }

    public String reason() {
        return this.reason;
    }
}

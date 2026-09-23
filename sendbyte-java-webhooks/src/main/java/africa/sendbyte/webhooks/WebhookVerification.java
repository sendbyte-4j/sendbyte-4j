package africa.sendbyte.webhooks;

import java.time.Instant;

/** Successful webhook signature verification details. */
public final class WebhookVerification {
    private final Instant signedAt;
    private final String matchedSignature;

    WebhookVerification(final Instant signedAt, final String matchedSignature) {
        this.signedAt = signedAt;
        this.matchedSignature = matchedSignature;
    }

    public Instant signedAt() {
        return this.signedAt;
    }

    public String matchedSignature() {
        return this.matchedSignature;
    }
}

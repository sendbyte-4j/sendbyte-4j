package africa.sendbyte.webhooks;

/** Thrown when a webhook signature cannot be verified. */
public final class WebhookVerificationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    WebhookVerificationException(final String message) {
        super(message);
    }
}

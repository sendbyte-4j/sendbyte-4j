package africa.sendbyte.webhooks;

import java.util.Locale;

/** Event type emitted by SendByte webhook deliveries. */
public enum WebhookEventType {
    EMAIL_SENT("email.sent"),
    EMAIL_DELIVERED("email.delivered"),
    EMAIL_OPENED("email.opened"),
    EMAIL_CLICKED("email.clicked"),
    EMAIL_BOUNCED("email.bounced"),
    EMAIL_COMPLAINED("email.complained"),
    EMAIL_UNSUBSCRIBED("email.unsubscribed"),
    EMAIL_FAILED("email.failed"),
    DOMAIN_VERIFIED("domain.verified"),
    DOMAIN_DEGRADED("domain.degraded"),
    SUPPRESSION_REMOVAL_REQUESTED("suppression.removal.requested"),
    SUPPRESSION_REMOVAL_VERIFIED("suppression.removal.verified"),
    SUPPRESSION_REMOVAL_COMPLETED("suppression.removal.completed"),
    SUPPRESSION_REMOVAL_EXPIRED("suppression.removal.expired"),
    SUPPRESSION_REMOVAL_FAILED("suppression.removal.failed"),
    SUPPRESSION_REMOVAL_CANCELLED("suppression.removal.cancelled"),
    SUPPRESSION_REMOVAL_REVERTED("suppression.removal.reverted"),
    UNKNOWN(null);

    private final String wireValue;

    WebhookEventType(final String wireValue) {
        this.wireValue = wireValue;
    }

    public String wireValue() {
        return this.wireValue;
    }

    static WebhookEventType fromWire(final String value) {
        final String normalized =
                value == null ? null : value.toUpperCase(Locale.ROOT).replace('.', '_');
        for (final WebhookEventType event : values()) {
            if (event.name().equals(normalized)) {
                return event;
            }
        }
        return UNKNOWN;
    }
}

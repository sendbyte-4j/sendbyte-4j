package africa.sendbyte.webhooks;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;

/** Deterministic helpers for tests that need signed SendByte webhook payloads. */
public final class WebhookTestFixtures {
    private WebhookTestFixtures() {}

    public static String signatureHeader(
            final String secret, final Instant timestamp, final byte[] body) {
        Objects.requireNonNull(timestamp, "timestamp");
        Objects.requireNonNull(body, "body");
        final long epochSecond = timestamp.getEpochSecond();
        return "t=" + epochSecond + ",v1=" + WebhookVerifier.sign(secret, epochSecond, body);
    }

    public static String signatureHeader(
            final String secret, final Instant timestamp, final String body) {
        return signatureHeader(secret, timestamp, body.getBytes(StandardCharsets.UTF_8));
    }

    public static String invalidSignatureHeader(final Instant timestamp) {
        Objects.requireNonNull(timestamp, "timestamp");
        return "t=" + timestamp.getEpochSecond() + ",v1=000000";
    }
}

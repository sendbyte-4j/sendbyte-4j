package africa.sendbyte.webhooks;

import africa.sendbyte.internal.WireSupport;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class WebhookVerifier {
    public static final Duration DEFAULT_TOLERANCE = Duration.ofMinutes(5);

    private final String secret;
    private final Duration tolerance;
    private final Clock clock;

    private WebhookVerifier(final Builder builder) {
        this.secret = WireSupport.requireText(builder.secret, "webhook signing secret");
        this.tolerance =
                Objects.requireNonNull(
                        builder.tolerance, "webhook timestamp tolerance must not be null");
        this.clock =
                Objects.requireNonNull(
                        builder.clock, "webhook verification clock must not be null");
        if (this.tolerance.isNegative()) {
            throw new IllegalArgumentException(
                    "webhook timestamp tolerance must be zero or positive");
        }
    }

    public static Builder builder(final String secret) {
        return new Builder(secret);
    }

    public WebhookVerification verify(final byte[] body, final String signatureHeader) {
        Objects.requireNonNull(body, "webhook request body must not be null");
        final ParsedHeader header = ParsedHeader.parse(signatureHeader);
        final Instant signedAt = Instant.ofEpochSecond(header.timestamp);
        final Duration age = Duration.between(signedAt, Instant.now(this.clock)).abs();
        if (age.compareTo(this.tolerance) > 0) {
            throw new WebhookVerificationException(
                    "webhook signature timestamp is outside the allowed tolerance of "
                            + this.tolerance);
        }

        final String expected = sign(this.secret, header.timestamp, body);
        for (final String signature : header.signatures) {
            if (secureEquals(expected, signature)) {
                return new WebhookVerification(signedAt, signature);
            }
        }
        throw new WebhookVerificationException("no v1 webhook signature matched the expected HMAC");
    }

    public boolean isValid(final byte[] body, final String signatureHeader) {
        try {
            verify(body, signatureHeader);
            return true;
        } catch (final WebhookVerificationException exception) {
            return false;
        }
    }

    static String sign(final String secret, final long timestamp, final byte[] body) {
        try {
            final Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            mac.update(Long.toString(timestamp).getBytes(StandardCharsets.UTF_8));
            mac.update((byte) '.');
            mac.update(body);
            return toHex(mac.doFinal());
        } catch (final java.security.GeneralSecurityException exception) {
            throw new IllegalStateException("HmacSHA256 is not available", exception);
        }
    }

    private static boolean secureEquals(final String expected, final String actual) {
        if (actual == null || expected.length() != actual.length()) {
            return false;
        }
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.US_ASCII),
                actual.getBytes(StandardCharsets.US_ASCII));
    }

    private static String toHex(final byte[] bytes) {
        final StringBuilder result = new StringBuilder(bytes.length * 2);
        for (final byte value : bytes) {
            final String hex = Integer.toHexString(value & 0xff);
            if (hex.length() == 1) {
                result.append('0');
            }
            result.append(hex);
        }
        return result.toString();
    }

    /** Mutable builder for {@link WebhookVerifier}. */
    public static final class Builder {
        private final String secret;
        private Duration tolerance = DEFAULT_TOLERANCE;
        private Clock clock = Clock.systemUTC();

        private Builder(final String secret) {
            this.secret = secret;
        }

        public Builder tolerance(final Duration tolerance) {
            this.tolerance = tolerance;
            return this;
        }

        public Builder clock(final Clock clock) {
            this.clock = clock;
            return this;
        }

        public WebhookVerifier build() {
            return new WebhookVerifier(this);
        }
    }

    private static final class ParsedHeader {
        private final long timestamp;
        private final List<String> signatures;

        private ParsedHeader(final long timestamp, final List<String> signatures) {
            this.timestamp = timestamp;
            this.signatures = signatures;
        }

        private static ParsedHeader parse(final String value) {
            WireSupport.requireText(value, "webhook signature header");
            Long timestamp = null;
            final List<String> signatures = new ArrayList<>();
            for (final String part : value.split(",")) {
                final int separator = part.indexOf('=');
                if (separator <= 0 || separator == part.length() - 1) {
                    throw new WebhookVerificationException(
                            "webhook signature header must contain comma-separated key/value pairs like t=<unix_seconds>,v1=<hex_hmac>");
                }
                final String key = part.substring(0, separator).trim();
                final String fieldValue = part.substring(separator + 1).trim();
                if ("t".equals(key)) {
                    timestamp = parseTimestamp(fieldValue);
                } else if ("v1".equals(key)) {
                    signatures.add(fieldValue);
                }
            }
            if (timestamp == null || signatures.isEmpty()) {
                throw new WebhookVerificationException(
                        "webhook signature header must include t=<unix_seconds> and at least one v1=<hex_hmac>");
            }
            return new ParsedHeader(timestamp.longValue(), signatures);
        }

        private static long parseTimestamp(final String value) {
            try {
                return Long.parseLong(value);
            } catch (final NumberFormatException exception) {
                throw new WebhookVerificationException(
                        "webhook signature timestamp must be a Unix timestamp in seconds");
            }
        }
    }
}

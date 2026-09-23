package africa.sendbyte.webhooks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

final class WebhookVerifierTest {
    private static final Instant NOW = Instant.parse("2026-09-23T07:00:00Z");
    private static final String SECRET = "whsec_test_secret";
    private static final byte[] BODY = "{\"id\":\"evt_1\"}".getBytes(StandardCharsets.UTF_8);

    @Test
    void verifiesRawBodyWithMultipleSignatures() {
        final WebhookVerifier verifier = verifierAt(NOW);
        final String valid =
                WebhookTestFixtures.signatureHeader(SECRET, NOW.minusSeconds(10), BODY);
        final String header =
                "t="
                        + NOW.minusSeconds(10).getEpochSecond()
                        + ",v1=000000,v1="
                        + valid.substring(valid.indexOf("v1=") + 3);

        final WebhookVerification verification = verifier.verify(BODY, header);

        assertEquals(NOW.minusSeconds(10), verification.signedAt());
        assertTrue(verifier.isValid(BODY, header));
    }

    @Test
    void rejectsChangedBodyMalformedHeaderAndExpiredTimestamp() {
        final WebhookVerifier verifier = verifierAt(NOW);
        final String header = WebhookTestFixtures.signatureHeader(SECRET, NOW, BODY);

        final WebhookVerificationException changedBody =
                assertThrows(
                        WebhookVerificationException.class,
                        () ->
                                verifier.verify(
                                        "{\"id\":\"evt_2\"}".getBytes(StandardCharsets.UTF_8),
                                        header));
        final WebhookVerificationException incompleteHeader =
                assertThrows(
                        WebhookVerificationException.class, () -> verifier.verify(BODY, "v1=abc"));
        final WebhookVerificationException expired =
                assertThrows(
                        WebhookVerificationException.class,
                        () ->
                                verifier.verify(
                                        BODY,
                                        WebhookTestFixtures.signatureHeader(
                                                SECRET, NOW.minus(Duration.ofMinutes(6)), BODY)));
        final WebhookVerificationException future =
                assertThrows(
                        WebhookVerificationException.class,
                        () ->
                                verifier.verify(
                                        BODY,
                                        WebhookTestFixtures.signatureHeader(
                                                SECRET, NOW.plus(Duration.ofMinutes(6)), BODY)));

        assertEquals("no v1 webhook signature matched the expected HMAC", changedBody.getMessage());
        assertEquals(
                "webhook signature header must include t=<unix_seconds> and at least one v1=<hex_hmac>",
                incompleteHeader.getMessage());
        assertEquals(
                "webhook signature timestamp is outside the allowed tolerance of PT5M",
                expired.getMessage());
        assertEquals(
                "webhook signature timestamp is outside the allowed tolerance of PT5M",
                future.getMessage());
        assertFalse(verifier.isValid(BODY, WebhookTestFixtures.invalidSignatureHeader(NOW)));
    }

    @Test
    void reportsInvalidVerifierConfigurationClearly() {
        final NullPointerException missingTolerance =
                assertThrows(
                        NullPointerException.class,
                        () -> WebhookVerifier.builder(SECRET).tolerance(null).build());
        final IllegalArgumentException negativeTolerance =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                WebhookVerifier.builder(SECRET)
                                        .tolerance(Duration.ofSeconds(-1))
                                        .build());

        assertEquals("webhook timestamp tolerance must not be null", missingTolerance.getMessage());
        assertEquals(
                "webhook timestamp tolerance must be zero or positive",
                negativeTolerance.getMessage());
    }

    private static WebhookVerifier verifierAt(final Instant instant) {
        return WebhookVerifier.builder(SECRET).clock(Clock.fixed(instant, ZoneOffset.UTC)).build();
    }
}

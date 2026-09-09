package africa.sendbyte.email;

import africa.sendbyte.ResourceOwnership;
import africa.sendbyte.SendByteClient;
import africa.sendbyte.exception.RateLimitException;
import africa.sendbyte.exception.ValidationException;
import africa.sendbyte.transport.HttpTransport;
import africa.sendbyte.transport.TransportRequest;
import africa.sendbyte.transport.TransportResponse;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EmailsTest {
    @Test
    void sendsContractRequestAndMapsImmutableResponse() {
        final CapturingTransport transport = new CapturingTransport(201,
                "{\"id\":\"em_123\",\"from\":\"sender@example.com\",\"to\":[\"to@example.com\"]," +
                        "\"subject\":\"Receipt\",\"status\":\"queued\",\"sandbox\":true," +
                        "\"tags\":[\"receipt\"],\"open_count\":0,\"click_count\":0," +
                        "\"created_at\":\"2026-09-09T10:15:30Z\"}");
        final SendByteClient client = client(transport);
        final Emails emails = new Emails(client);

        final SendEmailRequest request = SendEmailRequest.builder()
                .from("sender@example.com")
                .to("to@example.com")
                .replyTo("support@example.com")
                .subject("Receipt")
                .html("<p>Paid</p>")
                .attachment(Attachment.builder("receipt.txt", "text/plain",
                        "paid".getBytes(StandardCharsets.UTF_8)).build())
                .tag("receipt")
                .header("X-Campaign", "orders")
                .listUnsubscribe(ListUnsubscribe.of("https://example.com/unsubscribe", null))
                .scheduledAt(OffsetDateTime.parse("2026-09-10T09:00:00+01:00"))
                .idempotencyKey("order-123")
                .tracking(TrackingOptions.of(true, false))
                .build();

        final Email email = emails.send(request);

        assertEquals("POST", transport.request.method());
        assertEquals("https://api.test/v1/emails", transport.request.uri().toString());
        assertEquals("Bearer sk_test_example", first(transport.request, "Authorization"));
        assertEquals("application/json", first(transport.request, "Content-Type"));
        final String json = new String(transport.request.body(), StandardCharsets.UTF_8);
        assertTrue(json.contains("\"reply_to\":[\"support@example.com\"]"), json);
        assertTrue(json.contains("\"idempotency_key\":\"order-123\""), json);
        assertTrue(json.contains("\"content_type\":\"text/plain\""), json);
        assertTrue(json.contains("\"tracking\":{\"opens\":true,\"clicks\":false}"), json);
        assertFalse(json.contains("template_id"), json);

        assertEquals("em_123", email.id());
        assertEquals(EmailStatus.QUEUED, email.status());
        assertEquals("queued", email.rawStatus());
        assertTrue(email.sandbox());
        assertEquals(OffsetDateTime.parse("2026-09-09T10:15:30Z"), email.createdAt());
        assertThrows(UnsupportedOperationException.class, () -> email.to().add("other@example.com"));
    }

    @Test
    void mapsTypedErrorsAndResponseMetadata() {
        final Map<String, List<String>> headers = new LinkedHashMap<>();
        headers.put("X-Request-Id", Collections.singletonList("req_123"));
        final CapturingTransport validation = new CapturingTransport(422,
                "{\"error\":{\"code\":\"validation_error\",\"message\":\"subject is invalid\"," +
                        "\"docs_url\":\"https://docs.sendbyte.africa/errors/validation_error\"}}", headers);

        final ValidationException failure = assertThrows(ValidationException.class,
                () -> new Emails(client(validation)).send(minimalRequest()));

        assertEquals(422, failure.statusCode());
        assertEquals("validation_error", failure.code());
        assertEquals("req_123", failure.requestId());

        headers.put("Retry-After", Collections.singletonList("17"));
        final CapturingTransport limited = new CapturingTransport(429,
                "{\"error\":{\"code\":\"rate_limit_exceeded\",\"message\":\"slow down\"," +
                        "\"docs_url\":\"https://docs.sendbyte.africa/errors/rate_limit_exceeded\"}}", headers);
        final RateLimitException rateLimit = assertThrows(RateLimitException.class,
                () -> new Emails(client(limited)).send(minimalRequest()));
        assertEquals(Duration.ofSeconds(17), rateLimit.retryAfter());
    }

    @Test
    void propagatesAsyncCancellationToTransport() {
        final PendingTransport transport = new PendingTransport();
        final CompletableFuture<Email> send = new Emails(client(transport)).sendAsync(minimalRequest());

        assertTrue(send.cancel(true));

        assertTrue(transport.pending.isCancelled());
    }

    @Test
    void defaultJdkTransportPerformsRealLocalHttpRequest() throws Exception {
        final HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        final AtomicReference<String> authorization = new AtomicReference<>();
        final AtomicReference<String> body = new AtomicReference<>();

        server.createContext("/v1/emails", exchange -> {
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            final byte[] response = "{\"id\":\"em_local\",\"status\":\"queued\",\"created_at\":\"2026-09-09T10:15:30Z\"}"
                    .getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(201, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();

        try (SendByteClient client = SendByteClient.builder()
                .apiKey("sk_test_local")
                .baseUri(URI.create("http://127.0.0.1:" + server.getAddress().getPort()))
                .build()) {
            final Email email = new Emails(client).sendAsync(minimalRequest()).get(5, TimeUnit.SECONDS);

            assertEquals("em_local", email.id());
            assertEquals("Bearer sk_test_local", authorization.get());
            assertTrue(body.get().contains("\"subject\":\"Hello\""), body.get());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void validatesContractRequirementsBeforeTransport() {
        assertThrows(IllegalArgumentException.class, () -> SendEmailRequest.builder()
                .from("sender@example.com").to("to@example.com").text("body").build());
        assertThrows(IllegalArgumentException.class, () -> SendEmailRequest.builder()
                .from("sender@example.com").to("to@example.com").subject("subject").build());
        assertThrows(IllegalArgumentException.class, () -> SendEmailRequest.builder()
                .from("sender@example.com").to(Collections.nCopies(51, "to@example.com"))
                .subject("subject").text("body").build());
    }

    private static SendEmailRequest minimalRequest() {
        return SendEmailRequest.builder()
                .from("sender@example.com")
                .to("to@example.com")
                .subject("Hello")
                .text("Hello")
                .build();
    }

    private static SendByteClient client(final HttpTransport transport) {
        return SendByteClient.builder()
                .apiKey("sk_test_example")
                .baseUri(URI.create("https://api.test"))
                .transport(transport, ResourceOwnership.CALLER)
                .build();
    }

    private static String first(final TransportRequest request, final String header) {
        return request.headers().entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(header))
                .flatMap(entry -> entry.getValue().stream())
                .findFirst()
                .orElse(null);
    }

    private static final class CapturingTransport implements HttpTransport {
        private final int status;
        private final byte[] response;
        private final Map<String, List<String>> headers;
        private TransportRequest request;

        CapturingTransport(final int status, final String response) {
            this(status, response, Collections.emptyMap());
        }

        CapturingTransport(final int status, final String response, final Map<String, List<String>> headers) {
            this.status = status;
            this.response = response.getBytes(StandardCharsets.UTF_8);
            this.headers = headers;
        }

        @Override
        public TransportResponse execute(final TransportRequest request) {
            this.request = request;
            return new TransportResponse(this.status, this.headers, this.response);
        }

        @Override
        public CompletableFuture<TransportResponse> executeAsync(final TransportRequest request) {
            this.request = request;
            return CompletableFuture.completedFuture(
                    new TransportResponse(this.status, this.headers, this.response));
        }
    }

    private static final class PendingTransport implements HttpTransport {
        private final CompletableFuture<TransportResponse> pending = new CompletableFuture<>();

        @Override
        public TransportResponse execute(final TransportRequest request) throws IOException {
            throw new IOException("not used");
        }

        @Override
        public CompletableFuture<TransportResponse> executeAsync(final TransportRequest request) {
            return this.pending;
        }
    }
}

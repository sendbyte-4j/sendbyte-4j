package africa.sendbyte;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import africa.sendbyte.spi.ClientRequest;
import africa.sendbyte.transport.HttpTransport;
import africa.sendbyte.transport.TransportRequest;
import africa.sendbyte.transport.TransportResponse;
import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

final class SendByteClientTest {
    @Test
    void rejectsUnsafeOrInvalidConfiguration() {
        assertThrows(NullPointerException.class, () -> SendByteClient.builder().build());
        assertThrows(
                IllegalArgumentException.class, () -> SendByteClient.builder().apiKey(" ").build());
        assertThrows(
                IllegalArgumentException.class,
                () -> SendByteClient.builder().baseUri(URI.create("http://api.example.com")));
        assertThrows(
                IllegalArgumentException.class,
                () -> SendByteClient.builder().apiKey("key").requestTimeout(Duration.ZERO));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        SendByteClient.builder()
                                .apiKey("key")
                                .connectTimeout(Duration.ofSeconds(2))
                                .requestTimeout(Duration.ofSeconds(1))
                                .build());
        assertThrows(
                IllegalArgumentException.class,
                () -> SendByteClient.builder().apiKey("key").maxResponseBodyBytes(0));
    }

    @Test
    void closesOwnedTransportExactlyOnce() {
        final CloseCountingTransport transport = new CloseCountingTransport();
        final SendByteClient client =
                SendByteClient.builder()
                        .apiKey("sk_test_example")
                        .transport(transport, ResourceOwnership.CLIENT)
                        .build();

        client.close();
        client.close();

        assertTrue(client.isClosed());
        assertEquals(1, transport.closeCount.get());
        assertThrows(IllegalStateException.class, client::runtime);
    }

    @Test
    void leavesCallerOwnedTransportOpen() {
        final CloseCountingTransport transport = new CloseCountingTransport();
        final SendByteClient client =
                SendByteClient.builder()
                        .apiKey("sk_test_example")
                        .transport(transport, ResourceOwnership.CALLER)
                        .build();

        client.close();

        assertFalse(transport.closeCount.get() > 0);
    }

    @Test
    void encodesPathSegmentsAndQueryParameters() {
        final CapturingTransport transport = new CapturingTransport();
        final SendByteClient client =
                SendByteClient.builder()
                        .apiKey("sk_test_example")
                        .baseUri(URI.create("https://api.test"))
                        .transport(transport, ResourceOwnership.CALLER)
                        .build();

        client.runtime()
                .execute(
                        ClientRequest.builder("template.get", "GET")
                                .pathSegment("v1")
                                .pathSegment("templates")
                                .pathSegment("welcome/email + snow ☃?")
                                .queryParameter("search term", "a+b &/☃")
                                .queryParameter("tag", "receipt/new")
                                .build(),
                        Void.class);

        assertEquals(
                "https://api.test/v1/templates/welcome%2Femail%20%2B%20snow%20%E2%98%83%3F"
                        + "?search%20term=a%2Bb%20%26%2F%E2%98%83&tag=receipt%2Fnew",
                transport.request.uri().toString());
    }

    @Test
    void preservesCaseInsensitiveMultiValueHeaders() {
        final CapturingTransport transport = new CapturingTransport();
        final SendByteClient client =
                SendByteClient.builder()
                        .apiKey("sk_test_example")
                        .baseUri(URI.create("https://api.test"))
                        .transport(transport, ResourceOwnership.CALLER)
                        .build();

        client.runtime()
                .execute(
                        ClientRequest.builder("headers.get", "GET", "/v1/headers")
                                .header("authorization", "Bearer caller")
                                .header("X-SendByte-Test", "one")
                                .header("x-sendbyte-test", "two")
                                .build(),
                        Void.class);

        assertEquals(List.of("one", "two"), transport.request.headers().get("X-SENDBYTE-TEST"));
        assertEquals(
                Collections.singletonList("Bearer sk_test_example"),
                transport.request.headers().get("AUTHORIZATION"));
        assertThrows(
                UnsupportedOperationException.class,
                () -> transport.request.headers().get("x-sendbyte-test").add("three"));
    }

    @Test
    void responseHeadersAreCaseInsensitiveAndMultiValue() {
        final Map<String, List<String>> headers = new LinkedHashMap<>();
        headers.put("Retry-After", Collections.singletonList("17"));
        headers.put("retry-after", Collections.singletonList("29"));

        final TransportResponse response = new TransportResponse(429, headers, new byte[0]);

        assertEquals("17", response.firstHeader("RETRY-AFTER"));
        assertEquals(List.of("17", "29"), response.headers().get("retry-after"));
        assertThrows(
                UnsupportedOperationException.class,
                () -> response.headers().get("Retry-After").add("31"));
    }

    private static final class CloseCountingTransport implements HttpTransport {
        private final AtomicInteger closeCount = new AtomicInteger();

        @Override
        public TransportResponse execute(final TransportRequest request) {
            return new TransportResponse(204, Collections.emptyMap(), new byte[0]);
        }

        @Override
        public CompletableFuture<TransportResponse> executeAsync(final TransportRequest request) {
            return CompletableFuture.completedFuture(this.execute(request));
        }

        @Override
        public void close() {
            this.closeCount.incrementAndGet();
        }
    }

    private static final class CapturingTransport implements HttpTransport {
        private TransportRequest request;

        @Override
        public TransportResponse execute(final TransportRequest request) {
            this.request = request;
            return new TransportResponse(204, Collections.emptyMap(), new byte[0]);
        }

        @Override
        public CompletableFuture<TransportResponse> executeAsync(final TransportRequest request) {
            return CompletableFuture.completedFuture(this.execute(request));
        }
    }
}

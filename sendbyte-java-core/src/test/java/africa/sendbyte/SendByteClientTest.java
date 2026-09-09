package africa.sendbyte;

import africa.sendbyte.transport.HttpTransport;
import africa.sendbyte.transport.TransportRequest;
import africa.sendbyte.transport.TransportResponse;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SendByteClientTest {
    @Test
    void rejectsUnsafeOrInvalidConfiguration() {
        assertThrows(NullPointerException.class, () -> SendByteClient.builder().build());
        assertThrows(IllegalArgumentException.class, () -> SendByteClient.builder().apiKey(" ").build());
        assertThrows(IllegalArgumentException.class,
                () -> SendByteClient.builder().baseUri(URI.create("http://api.example.com")));
        assertThrows(IllegalArgumentException.class,
                () -> SendByteClient.builder().apiKey("key").requestTimeout(Duration.ZERO));
    }

    @Test
    void closesOwnedTransportExactlyOnce() {
        final CloseCountingTransport transport = new CloseCountingTransport();
        final SendByteClient client = SendByteClient.builder()
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
        final SendByteClient client = SendByteClient.builder()
                .apiKey("sk_test_example")
                .transport(transport, ResourceOwnership.CALLER)
                .build();

        client.close();

        assertFalse(transport.closeCount.get() > 0);
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
}

package africa.sendbyte.quarkus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import africa.sendbyte.RequestObserver;
import africa.sendbyte.ResourceOwnership;
import africa.sendbyte.SendByteClient;
import africa.sendbyte.spi.ClientRequest;
import africa.sendbyte.transport.HttpTransport;
import africa.sendbyte.transport.TransportRequest;
import africa.sendbyte.transport.TransportResponse;
import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

final class SendByteQuarkusClientFactoryTest {
    @Test
    void createsConfiguredClientWithTestTransportAndOrderedCustomizers() {
        final SendByteQuarkusConfig config = config();
        config.baseUri(URI.create("http://localhost:9999"));
        config.requestTimeout(Duration.ofSeconds(2));
        final CapturingTransport transport = new CapturingTransport();
        final AtomicInteger observed = new AtomicInteger();

        final SendByteClient client =
                new SendByteQuarkusClientFactory()
                        .create(
                                config,
                                null,
                                transport,
                                diagnostics -> observed.incrementAndGet(),
                                List.of(
                                        customizer(10, builder -> builder.userAgent("second")),
                                        customizer(-10, builder -> builder.userAgent("first"))));

        client.runtime()
                .execute(
                        ClientRequest.builder("test.ping", "GET")
                                .pathSegment("v1")
                                .pathSegment("ping")
                                .build(),
                        Void.class);

        assertEquals("http://localhost:9999/v1/ping", transport.request.get().uri().toString());
        assertEquals("second", transport.request.get().headers().get("User-Agent").get(0));
        assertEquals(1, observed.get());
    }

    @Test
    void reportsInvalidConfigurationWithoutLeakingApiKey() {
        final SendByteQuarkusConfig missing = new SendByteQuarkusConfig();
        final IllegalStateException missingFailure =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                new SendByteQuarkusClientFactory()
                                        .create(missing, null, null, null, List.of()));

        final SendByteQuarkusConfig invalid = config();
        invalid.connectTimeout(Duration.ofSeconds(5));
        invalid.requestTimeout(Duration.ofSeconds(1));
        final IllegalStateException invalidFailure =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                new SendByteQuarkusClientFactory()
                                        .create(invalid, null, null, null, List.of()));

        assertTrue(missingFailure.getMessage().contains("quarkus.sendbyte.api-key"));
        assertTrue(invalidFailure.getMessage().contains("Invalid SendByte Quarkus configuration"));
        assertFalse(invalidFailure.getMessage().contains("sk_test_example"));
    }

    @Test
    void usesUserProvidedClientAndOnlyClosesOwnedClients() {
        final AtomicInteger transportCloseCount = new AtomicInteger();
        final SendByteClient existing =
                SendByteClient.builder()
                        .apiKey("sk_test_existing")
                        .baseUri(URI.create("https://api.test"))
                        .transport(
                                new CountingTransport(transportCloseCount),
                                ResourceOwnership.CLIENT)
                        .build();
        final SendByteQuarkusClientFactory existingFactory = new SendByteQuarkusClientFactory();

        assertSame(existing, existingFactory.create(config(), existing, null, null, List.of()));
        existingFactory.close();
        assertFalse(existing.isClosed());
        assertEquals(0, transportCloseCount.get());

        final SendByteQuarkusClientFactory ownedFactory = new SendByteQuarkusClientFactory();
        final SendByteClient owned = ownedFactory.create(config(), null, null, null, List.of());
        ownedFactory.close();
        assertTrue(owned.isClosed());
    }

    @Test
    void createsEnabledProductFacadesAndRejectsDisabledProducts() {
        final SendByteQuarkusConfig config = config();
        final SendByteClient client =
                SendByteClient.builder()
                        .apiKey("sk_test_example")
                        .baseUri(URI.create("https://api.test"))
                        .transport(new CapturingTransport(), ResourceOwnership.CALLER)
                        .build();
        final SendByteQuarkusProductFactory products = new SendByteQuarkusProductFactory();

        assertNotNull(products.emails(client, config));
        assertNotNull(products.domains(client, config));
        assertNotNull(products.templates(client, config));
        assertNotNull(products.suppressions(client, config));
        assertNotNull(products.apiKeys(client, config));
        assertNotNull(products.marketing(client, config));
        assertNotNull(products.webhooks(client, config));

        config.email().enabled(false);
        assertThrows(IllegalStateException.class, () -> products.emails(client, config));
    }

    @Test
    void diagnosticsCanBeDisabled() {
        final SendByteQuarkusConfig config = config();
        config.diagnostics().enabled(false);
        final CapturingTransport transport = new CapturingTransport();
        final AtomicInteger observed = new AtomicInteger();
        final RequestObserver observer = diagnostics -> observed.incrementAndGet();
        final SendByteClient client =
                new SendByteQuarkusClientFactory()
                        .create(config, null, transport, observer, List.of());

        client.runtime()
                .execute(
                        ClientRequest.builder("test.ping", "GET")
                                .pathSegment("v1")
                                .pathSegment("ping")
                                .build(),
                        Void.class);

        assertEquals(0, observed.get());
        assertEquals(
                "https://api.sendbyte.africa/v1/ping", transport.request.get().uri().toString());
    }

    private static SendByteQuarkusConfig config() {
        final SendByteQuarkusConfig config = new SendByteQuarkusConfig();
        config.apiKey("sk_test_example");
        return config;
    }

    private static SendByteQuarkusClientBuilderCustomizer customizer(
            final int priority,
            final java.util.function.Consumer<SendByteClient.Builder> consumer) {
        return new SendByteQuarkusClientBuilderCustomizer() {
            @Override
            public void customize(final SendByteClient.Builder builder) {
                consumer.accept(builder);
            }

            @Override
            public int priority() {
                return priority;
            }
        };
    }

    private static class CapturingTransport implements HttpTransport {
        private final AtomicReference<TransportRequest> request = new AtomicReference<>();

        @Override
        public TransportResponse execute(final TransportRequest request) {
            this.request.set(request);
            return new TransportResponse(204, Collections.emptyMap(), new byte[0]);
        }

        @Override
        public CompletableFuture<TransportResponse> executeAsync(final TransportRequest request) {
            this.request.set(request);
            return CompletableFuture.completedFuture(
                    new TransportResponse(204, Collections.emptyMap(), new byte[0]));
        }
    }

    private static final class CountingTransport extends CapturingTransport {
        private final AtomicInteger closeCount;

        CountingTransport(final AtomicInteger closeCount) {
            this.closeCount = closeCount;
        }

        @Override
        public void close() {
            this.closeCount.incrementAndGet();
        }
    }
}

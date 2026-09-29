package africa.sendbyte.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import africa.sendbyte.RequestObserver;
import africa.sendbyte.ResourceOwnership;
import africa.sendbyte.SendByteClient;
import africa.sendbyte.admin.ApiKeys;
import africa.sendbyte.email.Domains;
import africa.sendbyte.email.Emails;
import africa.sendbyte.email.Suppressions;
import africa.sendbyte.email.Templates;
import africa.sendbyte.marketing.Marketing;
import africa.sendbyte.spi.ClientRequest;
import africa.sendbyte.transport.HttpTransport;
import africa.sendbyte.transport.TransportRequest;
import africa.sendbyte.transport.TransportResponse;
import africa.sendbyte.webhooks.Webhooks;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

final class SendByteAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(SendByteAutoConfiguration.class));

    @Test
    void createsClientAndPresentProductBeansFromApiKey() {
        contextRunner
                .withPropertyValues(
                        "sendbyte.api-key=sk_test_example", "sendbyte.base-uri=https://api.test")
                .run(
                        context -> {
                            assertNotNull(context.getBean(SendByteClient.class));
                            assertNotNull(context.getBean(Emails.class));
                            assertNotNull(context.getBean(Domains.class));
                            assertNotNull(context.getBean(Templates.class));
                            assertNotNull(context.getBean(Suppressions.class));
                            assertNotNull(context.getBean(ApiKeys.class));
                            assertNotNull(context.getBean(Marketing.class));
                            assertNotNull(context.getBean(Webhooks.class));
                        });
    }

    @Test
    void failsWithActionableMessagesForMissingAndInvalidConfiguration() {
        contextRunner.run(
                context -> {
                    assertNotNull(context.getStartupFailure());
                    assertTrue(
                            context.getStartupFailure().getMessage().contains("sendbyte.api-key"));
                });

        contextRunner
                .withPropertyValues(
                        "sendbyte.api-key=sk_test_example",
                        "sendbyte.base-uri=https://api.test",
                        "sendbyte.connect-timeout=5s",
                        "sendbyte.request-timeout=1s")
                .run(
                        context -> {
                            assertNotNull(context.getStartupFailure());
                            assertTrue(
                                    context.getStartupFailure()
                                            .getMessage()
                                            .contains("Invalid SendByte configuration"));
                            assertFalse(
                                    context.getStartupFailure()
                                            .getMessage()
                                            .contains("sk_test_example"));
                        });
    }

    @Test
    void backsOffForUserProvidedClientAndProductBeans() {
        contextRunner
                .withPropertyValues(
                        "sendbyte.api-key=sk_test_example", "sendbyte.base-uri=https://api.test")
                .withUserConfiguration(UserClientConfiguration.class, UserEmailConfiguration.class)
                .run(
                        context -> {
                            assertSame(
                                    UserClientConfiguration.CLIENT,
                                    context.getBean(SendByteClient.class));
                            assertSame(
                                    UserEmailConfiguration.EMAILS, context.getBean(Emails.class));
                        });
    }

    @Test
    void productBeansCanBeDisabledOrMissingFromTheClasspath() {
        contextRunner
                .withPropertyValues(
                        "sendbyte.api-key=sk_test_example",
                        "sendbyte.base-uri=https://api.test",
                        "sendbyte.email.enabled=false")
                .run(
                        context -> {
                            assertFalse(context.containsBean("emails"));
                            assertFalse(context.containsBean("domains"));
                            assertTrue(context.containsBean("apiKeys"));
                        });

        contextRunner
                .withClassLoader(new FilteredClassLoader("africa.sendbyte.email"))
                .withPropertyValues(
                        "sendbyte.api-key=sk_test_example", "sendbyte.base-uri=https://api.test")
                .run(
                        context -> {
                            assertFalse(context.containsBean("emails"));
                            assertFalse(context.containsBean("domains"));
                        });
    }

    @Test
    void fakeTransportAndBaseUriCanBeSuppliedForTests() {
        final CapturingTransport transport = new CapturingTransport();
        contextRunner
                .withPropertyValues(
                        "sendbyte.api-key=sk_test_example",
                        "sendbyte.base-uri=http://localhost:9999",
                        "sendbyte.request-timeout=2s",
                        "sendbyte.user-agent=test-agent")
                .withBean(HttpTransport.class, () -> transport)
                .run(
                        context -> {
                            final SendByteClient client = context.getBean(SendByteClient.class);
                            client.runtime()
                                    .execute(
                                            ClientRequest.builder("test.ping", "GET")
                                                    .pathSegment("v1")
                                                    .pathSegment("ping")
                                                    .build(),
                                            Void.class);

                            final TransportRequest request = transport.request.get();
                            assertEquals("http://localhost:9999/v1/ping", request.uri().toString());
                            assertTrue(request.timeout().compareTo(Duration.ofSeconds(2)) <= 0);
                            assertTrue(request.timeout().compareTo(Duration.ofSeconds(1)) > 0);
                            assertEquals("test-agent", request.headers().get("User-Agent").get(0));
                        });
    }

    @Test
    void appliesOrderedCustomizersAndClosesOwnedResourcesOnce() {
        final AtomicInteger closeCount = new AtomicInteger();
        final CountingTransport transport = new CountingTransport(closeCount);

        contextRunner
                .withPropertyValues(
                        "sendbyte.api-key=sk_test_example", "sendbyte.base-uri=https://api.test")
                .withBean(
                        "secondCustomizer",
                        SendByteClientBuilderCustomizer.class,
                        () -> builder -> builder.userAgent("second"))
                .withBean(
                        "firstCustomizer",
                        SendByteClientBuilderCustomizer.class,
                        () ->
                                new OrderedCustomizer(
                                        -1,
                                        builder ->
                                                builder.transport(
                                                        transport, ResourceOwnership.CLIENT)))
                .run(context -> assertNotNull(context.getBean(SendByteClient.class)));

        assertEquals(1, closeCount.get());
    }

    @Test
    void diagnosticsCanBeDisabledEvenWhenObserverBeanExists() {
        final AtomicInteger observed = new AtomicInteger();
        final CapturingTransport transport = new CapturingTransport();
        contextRunner
                .withPropertyValues(
                        "sendbyte.api-key=sk_test_example",
                        "sendbyte.base-uri=https://api.test",
                        "sendbyte.diagnostics.enabled=false")
                .withBean(HttpTransport.class, () -> transport)
                .withBean(RequestObserver.class, () -> diagnostics -> observed.incrementAndGet())
                .run(
                        context -> {
                            context.getBean(SendByteClient.class)
                                    .runtime()
                                    .execute(
                                            ClientRequest.builder("test.ping", "GET")
                                                    .pathSegment("v1")
                                                    .pathSegment("ping")
                                                    .build(),
                                            Void.class);
                            assertEquals(0, observed.get());
                        });
    }

    @Test
    void configurationMetadataIsGenerated() throws IOException {
        final byte[] metadata =
                getClass()
                        .getClassLoader()
                        .getResourceAsStream("META-INF/spring-configuration-metadata.json")
                        .readAllBytes();
        final String json = new String(metadata, StandardCharsets.UTF_8);

        assertTrue(json.contains("sendbyte.api-key"));
        assertTrue(json.contains("sendbyte.retry.max-attempts"));
        assertTrue(json.contains("sendbyte.email.enabled"));
    }

    @Configuration(proxyBeanMethods = false)
    static class UserClientConfiguration {
        static final SendByteClient CLIENT =
                SendByteClient.builder()
                        .apiKey("sk_test_user")
                        .baseUri(URI.create("https://api.test"))
                        .transport(new CapturingTransport(), ResourceOwnership.CALLER)
                        .build();

        @Bean
        SendByteClient sendByteClient() {
            return CLIENT;
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class UserEmailConfiguration {
        static final Emails EMAILS = new Emails(UserClientConfiguration.CLIENT);

        @Bean
        Emails emails() {
            return EMAILS;
        }
    }

    private static final class OrderedCustomizer
            implements SendByteClientBuilderCustomizer, org.springframework.core.Ordered {
        private final int order;
        private final SendByteClientBuilderCustomizer delegate;

        OrderedCustomizer(final int order, final SendByteClientBuilderCustomizer delegate) {
            this.order = order;
            this.delegate = delegate;
        }

        @Override
        public int getOrder() {
            return this.order;
        }

        @Override
        public void customize(final SendByteClient.Builder builder) {
            this.delegate.customize(builder);
        }
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

package africa.sendbyte.quarkus;

import africa.sendbyte.RequestObserver;
import africa.sendbyte.ResourceOwnership;
import africa.sendbyte.RetryPolicy;
import africa.sendbyte.SendByteClient;
import africa.sendbyte.transport.HttpTransport;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Creates and owns the SendByte client used by the Quarkus integration. */
public final class SendByteQuarkusClientFactory implements AutoCloseable {
    private SendByteClient client;
    private boolean ownsClient;

    public SendByteClient create(
            final SendByteQuarkusConfig config,
            final SendByteClient existingClient,
            final HttpTransport transport,
            final RequestObserver observer,
            final List<SendByteQuarkusClientBuilderCustomizer> customizers) {
        Objects.requireNonNull(config, "config");
        if (!config.enabled()) {
            throw new IllegalStateException("quarkus.sendbyte.enabled is false");
        }
        if (existingClient != null) {
            this.client = existingClient;
            this.ownsClient = false;
            return existingClient;
        }
        validateRequiredConfig(config);

        final SendByteClient.Builder builder =
                SendByteClient.builder()
                        .apiKey(config.apiKey())
                        .baseUri(config.baseUri())
                        .connectTimeout(config.connectTimeout())
                        .requestTimeout(config.requestTimeout())
                        .maxResponseBodyBytes(config.maxResponseBodyBytes())
                        .retryPolicy(retryPolicy(config.retry()));
        if (config.userAgent() != null) {
            builder.userAgent(config.userAgent());
        }
        if (transport != null) {
            builder.transport(transport, ResourceOwnership.CALLER);
        }
        if (observer != null && config.diagnostics().enabled()) {
            builder.requestObserver(observer);
        }

        final List<SendByteQuarkusClientBuilderCustomizer> ordered =
                customizers == null ? List.of() : new ArrayList<>(customizers);
        ordered.stream()
                .sorted(Comparator.comparingInt(SendByteQuarkusClientBuilderCustomizer::priority))
                .forEach(customizer -> customizer.customize(builder));

        try {
            this.client = builder.build();
            this.ownsClient = true;
            return this.client;
        } catch (final IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "Invalid SendByte Quarkus configuration: " + exception.getMessage(), exception);
        }
    }

    public SendByteClient client() {
        if (this.client == null) {
            throw new IllegalStateException("SendByte Quarkus client has not been created");
        }
        return this.client;
    }

    @Override
    public void close() {
        if (this.ownsClient && this.client != null) {
            this.client.close();
        }
    }

    private static void validateRequiredConfig(final SendByteQuarkusConfig config) {
        if (config.apiKey() == null || config.apiKey().trim().isEmpty()) {
            throw new IllegalStateException(
                    "quarkus.sendbyte.api-key must be configured with a non-blank SendByte API key");
        }
    }

    private static RetryPolicy retryPolicy(final SendByteQuarkusConfig.Retry retry) {
        Objects.requireNonNull(retry, "retry");
        return RetryPolicy.builder()
                .maxAttempts(retry.maxAttempts())
                .initialBackoff(retry.initialBackoff())
                .maxBackoff(retry.maxBackoff())
                .jitterRatio(retry.jitterRatio())
                .build();
    }
}

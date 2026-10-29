package africa.sendbyte.spring;

import africa.sendbyte.RequestObserver;
import africa.sendbyte.ResourceOwnership;
import africa.sendbyte.RetryPolicy;
import africa.sendbyte.SendByteClient;
import africa.sendbyte.admin.ApiKeys;
import africa.sendbyte.email.Domains;
import africa.sendbyte.email.Emails;
import africa.sendbyte.email.Suppressions;
import africa.sendbyte.email.Templates;
import africa.sendbyte.marketing.Marketing;
import africa.sendbyte.transport.HttpTransport;
import africa.sendbyte.webhooks.Webhooks;
import java.util.Objects;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/** Auto-configuration for the SendByte Java SDK. */
@AutoConfiguration
@EnableConfigurationProperties(SendByteProperties.class)
@ConditionalOnProperty(
        prefix = "sendbyte",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
@Import({
    SendByteAutoConfiguration.EmailAutoConfiguration.class,
    SendByteAutoConfiguration.AdminAutoConfiguration.class,
    SendByteAutoConfiguration.MarketingAutoConfiguration.class,
    SendByteAutoConfiguration.WebhooksAutoConfiguration.class
})
public class SendByteAutoConfiguration {
    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean
    public SendByteClient sendByteClient(
            final SendByteProperties properties,
            final ObjectProvider<HttpTransport> transport,
            final ObjectProvider<RequestObserver> observer,
            final ObjectProvider<SendByteClientBuilderCustomizer> customizers) {
        validateRequiredProperties(properties);

        final SendByteClient.Builder builder =
                SendByteClient.builder()
                        .apiKey(properties.getApiKey())
                        .baseUri(properties.getBaseUri())
                        .connectTimeout(properties.getConnectTimeout())
                        .requestTimeout(properties.getRequestTimeout())
                        .maxResponseBodyBytes(properties.getMaxResponseBodyBytes())
                        .retryPolicy(retryPolicy(properties.getRetry()));
        if (properties.getUserAgent() != null) {
            builder.userAgent(properties.getUserAgent());
        }
        final HttpTransport configuredTransport = transport.getIfUnique();
        if (configuredTransport != null) {
            builder.transport(configuredTransport, ResourceOwnership.CALLER);
        }
        final RequestObserver configuredObserver = observer.getIfUnique();
        if (configuredObserver != null && properties.getDiagnostics().isEnabled()) {
            builder.requestObserver(configuredObserver);
        }

        customizers.orderedStream().forEach(customizer -> customizer.customize(builder));

        try {
            return builder.build();
        } catch (final IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "Invalid SendByte configuration: " + exception.getMessage(), exception);
        }
    }

    private static void validateRequiredProperties(final SendByteProperties properties) {
        if (properties.getApiKey() == null || properties.getApiKey().trim().isEmpty()) {
            throw new IllegalStateException(
                    "sendbyte.api-key must be configured with a non-blank SendByte API key");
        }
    }

    private static RetryPolicy retryPolicy(final SendByteProperties.Retry properties) {
        Objects.requireNonNull(properties, "properties");
        return RetryPolicy.builder()
                .maxAttempts(properties.getMaxAttempts())
                .initialBackoff(properties.getInitialBackoff())
                .maxBackoff(properties.getMaxBackoff())
                .jitterRatio(properties.getJitterRatio())
                .build();
    }

    @AutoConfiguration
    @ConditionalOnClass(Emails.class)
    @ConditionalOnProperty(
            prefix = "sendbyte.email",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    static class EmailAutoConfiguration {
        @Bean
        @ConditionalOnMissingBean
        Emails emails(final SendByteClient client) {
            return new Emails(client);
        }

        @Bean
        @ConditionalOnMissingBean
        Domains domains(final SendByteClient client) {
            return new Domains(client);
        }

        @Bean
        @ConditionalOnMissingBean
        Templates templates(final SendByteClient client) {
            return new Templates(client);
        }

        @Bean
        @ConditionalOnMissingBean
        Suppressions suppressions(final SendByteClient client) {
            return new Suppressions(client);
        }
    }

    @AutoConfiguration
    @ConditionalOnClass(ApiKeys.class)
    @ConditionalOnProperty(
            prefix = "sendbyte.admin",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    static class AdminAutoConfiguration {
        @Bean
        @ConditionalOnMissingBean
        ApiKeys apiKeys(final SendByteClient client) {
            return new ApiKeys(client);
        }
    }

    @AutoConfiguration
    @ConditionalOnClass(Marketing.class)
    @ConditionalOnProperty(
            prefix = "sendbyte.marketing",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    static class MarketingAutoConfiguration {
        @Bean
        @ConditionalOnMissingBean
        Marketing marketing(final SendByteClient client) {
            return new Marketing(client);
        }
    }

    @AutoConfiguration
    @ConditionalOnClass(Webhooks.class)
    @ConditionalOnProperty(
            prefix = "sendbyte.webhooks",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    static class WebhooksAutoConfiguration {
        @Bean
        @ConditionalOnMissingBean
        Webhooks webhooks(final SendByteClient client) {
            return new Webhooks(client);
        }
    }
}

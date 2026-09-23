package africa.sendbyte.spring;

import africa.sendbyte.SendByteClient;

/** Customizes the auto-configured {@link SendByteClient.Builder}. */
@FunctionalInterface
public interface SendByteClientBuilderCustomizer {
    void customize(SendByteClient.Builder builder);
}

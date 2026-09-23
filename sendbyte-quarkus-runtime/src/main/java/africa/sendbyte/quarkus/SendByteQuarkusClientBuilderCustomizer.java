package africa.sendbyte.quarkus;

import africa.sendbyte.SendByteClient;

/** Quarkus integration hook for customizing the SendByte client builder. */
@FunctionalInterface
public interface SendByteQuarkusClientBuilderCustomizer {
    void customize(SendByteClient.Builder builder);

    default int priority() {
        return 0;
    }
}

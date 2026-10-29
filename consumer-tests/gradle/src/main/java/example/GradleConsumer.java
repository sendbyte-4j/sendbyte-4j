package example;

import africa.sendbyte.SendByteClient;
import africa.sendbyte.email.Emails;

public final class GradleConsumer {
    private GradleConsumer() {}

    public static Emails emails(final String apiKey) {
        final SendByteClient client = SendByteClient.builder().apiKey(apiKey).build();
        return new Emails(client);
    }
}

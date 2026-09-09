package example;

import africa.sendbyte.SendByteClient;
import africa.sendbyte.email.Emails;

public final class SelectiveConsumer {
    private SelectiveConsumer() {}

    public static Emails emails(final String apiKey) {
        final SendByteClient client = SendByteClient.builder().apiKey(apiKey).build();
        return new Emails(client);
    }
}

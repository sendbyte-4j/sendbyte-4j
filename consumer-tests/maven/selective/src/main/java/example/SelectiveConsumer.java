package example;

import africa.sendbyte.SendByteClient;
import africa.sendbyte.email.Emails;
import java.net.URI;

public final class SelectiveConsumer {
    private SelectiveConsumer() {}

    public static Emails emails(final String apiKey) {
        final SendByteClient client =
                SendByteClient.builder()
                        .apiKey(apiKey)
                        .baseUri(URI.create("https://api.example.test"))
                        .build();
        return new Emails(client);
    }
}

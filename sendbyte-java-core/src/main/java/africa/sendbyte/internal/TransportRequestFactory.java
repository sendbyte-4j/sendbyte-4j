package africa.sendbyte.internal;

import africa.sendbyte.CredentialsProvider;
import africa.sendbyte.spi.ClientRequest;
import africa.sendbyte.transport.TransportRequest;
import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

final class TransportRequestFactory {
    private final URI baseUri;
    private final CredentialsProvider credentialsProvider;
    private final String userAgent;
    private final JacksonJsonCodec json;

    TransportRequestFactory(
            final URI baseUri,
            final CredentialsProvider credentialsProvider,
            final String userAgent,
            final JacksonJsonCodec json) {
        this.baseUri = baseUri;
        this.credentialsProvider = credentialsProvider;
        this.userAgent = userAgent;
        this.json = json;
    }

    TransportRequest create(final ClientRequest request, final Duration remainingTimeout) {
        final byte[] body = request.body() == null ? new byte[0] : this.json.write(request.body());
        final Map<String, List<String>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        request.headers().forEach((name, values) -> headers.put(name, List.copyOf(values)));
        headers.put("Authorization", Collections.singletonList("Bearer " + this.currentApiKey()));
        headers.put("Accept", Collections.singletonList("application/json"));
        headers.put("User-Agent", Collections.singletonList(this.userAgent));

        if (request.body() != null) {
            headers.put("Content-Type", Collections.singletonList("application/json"));
        }

        return new TransportRequest(
                request.method(),
                this.baseUri.resolve(request.path()),
                headers,
                body,
                remainingTimeout);
    }

    private String currentApiKey() {
        final String apiKey = this.credentialsProvider.apiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("The credentials provider returned a blank API key");
        }

        return apiKey;
    }
}

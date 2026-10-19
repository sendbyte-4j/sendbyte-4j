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

    TransportRequestFactory(
            final URI baseUri,
            final CredentialsProvider credentialsProvider,
            final String userAgent) {
        this.baseUri = baseUri;
        this.credentialsProvider = credentialsProvider;
        this.userAgent = userAgent;
    }

    /**
     * Assembles the transport request from an already encoded body. Encoding is the caller's job so
     * that it can happen once per execution rather than once per attempt.
     *
     * @param body encoded request body, or null when the operation sends no body
     */
    TransportRequest create(
            final ClientRequest request, final byte[] body, final Duration remainingTimeout) {
        final Map<String, List<String>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        request.headers().forEach((name, values) -> headers.put(name, List.copyOf(values)));
        headers.put("Authorization", Collections.singletonList("Bearer " + this.currentApiKey()));
        headers.put("Accept", Collections.singletonList("application/json"));
        headers.put("User-Agent", Collections.singletonList(userAgent));

        if (body != null) {
            headers.put("Content-Type", Collections.singletonList("application/json"));
        }

        return new TransportRequest(
                request.method(), baseUri.resolve(request.path()), headers, body, remainingTimeout);
    }

    private String currentApiKey() {
        final String apiKey = credentialsProvider.apiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("The credentials provider returned a blank API key");
        }

        return apiKey;
    }
}

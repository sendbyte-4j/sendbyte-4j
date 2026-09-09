package africa.sendbyte.transport;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** Default transport backed by the Java 11 HTTP client. */
public final class JdkHttpTransport implements HttpTransport {
    private final HttpClient client;

    public JdkHttpTransport(final Duration connectTimeout) {
        this(HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build());
    }

    public JdkHttpTransport(final HttpClient client) {
        this.client = java.util.Objects.requireNonNull(client, "client");
    }

    @Override
    public TransportResponse execute(final TransportRequest request) throws IOException, InterruptedException {
        return adapt(this.client.send(toJdkRequest(request), HttpResponse.BodyHandlers.ofByteArray()));
    }

    @Override
    public CompletableFuture<TransportResponse> executeAsync(final TransportRequest request) {
        final CompletableFuture<HttpResponse<byte[]>> source = this.client.sendAsync(
                toJdkRequest(request), HttpResponse.BodyHandlers.ofByteArray());
        final CompletableFuture<TransportResponse> result = new CompletableFuture<>();

        source.whenComplete((response, failure) -> {
            if (failure != null) {
                result.completeExceptionally(failure);
            } else {
                result.complete(adapt(response));
            }
        });
        result.whenComplete((ignored, failure) -> {
            if (result.isCancelled()) {
                source.cancel(true);
            }
        });
        return result;
    }

    private static HttpRequest toJdkRequest(final TransportRequest request) {
        final HttpRequest.Builder builder = HttpRequest.newBuilder(request.uri())
                .timeout(request.timeout())
                .method(request.method(), HttpRequest.BodyPublishers.ofByteArray(request.body()));

        for (final Map.Entry<String, java.util.List<String>> header : request.headers().entrySet()) {
            for (final String value : header.getValue()) {
                builder.header(header.getKey(), value);
            }
        }

        return builder.build();
    }

    private static TransportResponse adapt(final HttpResponse<byte[]> response) {
        return new TransportResponse(response.statusCode(), response.headers().map(), response.body());
    }
}

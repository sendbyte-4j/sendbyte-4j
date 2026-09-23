package africa.sendbyte.email;

import africa.sendbyte.Page;
import africa.sendbyte.SendByteClient;
import africa.sendbyte.spi.ClientRequest;
import africa.sendbyte.spi.ClientRuntime;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/** Suppression and suppression-removal operations. */
public final class Suppressions {
    private final ClientRuntime runtime;

    public Suppressions(final SendByteClient client) {
        runtime = Objects.requireNonNull(client, "client").runtime();
    }

    public Page<Suppression> list(final ListSuppressionsRequest request) {
        Objects.requireNonNull(request, "request");

        return runtime.execute(listRequest(request), SuppressionPageApiResponse.class).toPage();
    }

    public CompletableFuture<Page<Suppression>> listAsync(final ListSuppressionsRequest request) {
        Objects.requireNonNull(request, "request");

        return runtime.executeAsync(listRequest(request), SuppressionPageApiResponse.class)
                .thenApply(SuppressionPageApiResponse::toPage);
    }

    public Suppression get(final String address) {
        return runtime.execute(getRequest(address), SuppressionApiResponse.class).toSuppression();
    }

    public CompletableFuture<Suppression> getAsync(final String address) {
        return runtime.executeAsync(getRequest(address), SuppressionApiResponse.class)
                .thenApply(SuppressionApiResponse::toSuppression);
    }

    public SuppressionRemoval requestRemoval(final CreateSuppressionRemovalRequest request) {
        Objects.requireNonNull(request, "request");

        return runtime.execute(removalCreateRequest(request), SuppressionRemovalApiResponse.class)
                .toSuppressionRemoval();
    }

    public CompletableFuture<SuppressionRemoval> requestRemovalAsync(
            final CreateSuppressionRemovalRequest request) {
        Objects.requireNonNull(request, "request");

        return runtime.executeAsync(
                        removalCreateRequest(request), SuppressionRemovalApiResponse.class)
                .thenApply(SuppressionRemovalApiResponse::toSuppressionRemoval);
    }

    public Page<SuppressionRemoval> listRemovals() {
        return runtime.execute(
                        removalCollectionRequest("suppression.removal.list", "GET", null),
                        SuppressionRemovalPageApiResponse.class)
                .toPage();
    }

    public CompletableFuture<Page<SuppressionRemoval>> listRemovalsAsync() {
        return runtime.executeAsync(
                        removalCollectionRequest("suppression.removal.list", "GET", null),
                        SuppressionRemovalPageApiResponse.class)
                .thenApply(SuppressionRemovalPageApiResponse::toPage);
    }

    public SuppressionRemoval getRemoval(final String id) {
        return runtime.execute(
                        removalItemRequest("suppression.removal.get", "GET", id),
                        SuppressionRemovalApiResponse.class)
                .toSuppressionRemoval();
    }

    public CompletableFuture<SuppressionRemoval> getRemovalAsync(final String id) {
        return runtime.executeAsync(
                        removalItemRequest("suppression.removal.get", "GET", id),
                        SuppressionRemovalApiResponse.class)
                .thenApply(SuppressionRemovalApiResponse::toSuppressionRemoval);
    }

    public SuppressionRemoval cancelRemoval(final String id) {
        return runtime.execute(
                        removalItemRequest("suppression.removal.cancel", "DELETE", id),
                        SuppressionRemovalApiResponse.class)
                .toSuppressionRemoval();
    }

    public CompletableFuture<SuppressionRemoval> cancelRemovalAsync(final String id) {
        return runtime.executeAsync(
                        removalItemRequest("suppression.removal.cancel", "DELETE", id),
                        SuppressionRemovalApiResponse.class)
                .thenApply(SuppressionRemovalApiResponse::toSuppressionRemoval);
    }

    private static ClientRequest listRequest(final ListSuppressionsRequest request) {
        final ClientRequest.Builder builder =
                ClientRequest.builder("suppression.list", "GET")
                        .pathSegment("v1")
                        .pathSegment("suppressions");
        if (request.search() != null) {
            builder.queryParameter("search", request.search());
        }
        if (request.limit() != null) {
            builder.queryParameter("limit", request.limit().toString());
        }
        return builder.build();
    }

    private static ClientRequest getRequest(final String address) {
        return ClientRequest.builder("suppression.get", "GET")
                .pathSegment("v1")
                .pathSegment("suppressions")
                .pathSegment(Emails.requireId(address, "address"))
                .build();
    }

    private static ClientRequest removalCreateRequest(
            final CreateSuppressionRemovalRequest request) {
        return removalCollectionRequest(
                "suppression.removal.create", "POST", SuppressionRemovalApiRequest.from(request));
    }

    private static ClientRequest removalCollectionRequest(
            final String operation, final String method, final Object body) {
        final ClientRequest.Builder builder =
                ClientRequest.builder(operation, method)
                        .pathSegment("v1")
                        .pathSegment("suppressions")
                        .pathSegment("removals");
        if (body != null) {
            builder.body(body);
        }
        return builder.build();
    }

    private static ClientRequest removalItemRequest(
            final String operation, final String method, final String id) {
        return ClientRequest.builder(operation, method)
                .pathSegment("v1")
                .pathSegment("suppressions")
                .pathSegment("removals")
                .pathSegment(Emails.requireId(id, "id"))
                .build();
    }
}

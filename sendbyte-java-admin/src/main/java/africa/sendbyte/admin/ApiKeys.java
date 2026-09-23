package africa.sendbyte.admin;

import africa.sendbyte.Page;
import africa.sendbyte.SendByteClient;
import africa.sendbyte.spi.ClientRequest;
import africa.sendbyte.spi.ClientRuntime;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/** Admin API-key operations. */
public final class ApiKeys {
    private final ClientRuntime runtime;

    public ApiKeys(final SendByteClient client) {
        runtime = Objects.requireNonNull(client, "client").runtime();
    }

    public ApiKey create(final CreateApiKeyRequest request) {
        Objects.requireNonNull(request, "request");

        return runtime.execute(
                        collectionRequest("api_key.create", "POST", ApiKeyApiRequest.from(request)),
                        ApiKeyApiResponse.class)
                .toApiKey();
    }

    public CompletableFuture<ApiKey> createAsync(final CreateApiKeyRequest request) {
        Objects.requireNonNull(request, "request");

        return runtime.executeAsync(
                        collectionRequest("api_key.create", "POST", ApiKeyApiRequest.from(request)),
                        ApiKeyApiResponse.class)
                .thenApply(ApiKeyApiResponse::toApiKey);
    }

    public Page<ApiKey> list() {
        return runtime.execute(
                        collectionRequest("api_key.list", "GET", null), ApiKeyPageApiResponse.class)
                .toPage();
    }

    public CompletableFuture<Page<ApiKey>> listAsync() {
        return runtime.executeAsync(
                        collectionRequest("api_key.list", "GET", null), ApiKeyPageApiResponse.class)
                .thenApply(ApiKeyPageApiResponse::toPage);
    }

    public ApiKey rename(final String id, final String name) {
        return runtime.execute(
                        itemRequest("api_key.rename", "PATCH", id, ApiKeyApiRequest.rename(name)),
                        ApiKeyApiResponse.class)
                .toApiKey();
    }

    public CompletableFuture<ApiKey> renameAsync(final String id, final String name) {
        return runtime.executeAsync(
                        itemRequest("api_key.rename", "PATCH", id, ApiKeyApiRequest.rename(name)),
                        ApiKeyApiResponse.class)
                .thenApply(ApiKeyApiResponse::toApiKey);
    }

    public ApiKey revoke(final String id) {
        return runtime.execute(
                        itemRequest("api_key.revoke", "DELETE", id, null), ApiKeyApiResponse.class)
                .toApiKey();
    }

    public CompletableFuture<ApiKey> revokeAsync(final String id) {
        return runtime.executeAsync(
                        itemRequest("api_key.revoke", "DELETE", id, null), ApiKeyApiResponse.class)
                .thenApply(ApiKeyApiResponse::toApiKey);
    }

    private static ClientRequest collectionRequest(
            final String operation, final String method, final Object body) {
        final ClientRequest.Builder builder =
                ClientRequest.builder(operation, method).pathSegment("v1").pathSegment("api-keys");
        if (body != null) {
            builder.body(body);
        }
        return builder.build();
    }

    private static ClientRequest itemRequest(
            final String operation, final String method, final String id, final Object body) {
        final ClientRequest.Builder builder =
                ClientRequest.builder(operation, method)
                        .pathSegment("v1")
                        .pathSegment("api-keys")
                        .pathSegment(requireId(id));
        if (body != null) {
            builder.body(body);
        }
        return builder.build();
    }

    private static String requireId(final String value) {
        Objects.requireNonNull(value, "id");
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        return value;
    }
}

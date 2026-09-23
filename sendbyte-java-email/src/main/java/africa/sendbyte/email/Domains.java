package africa.sendbyte.email;

import africa.sendbyte.Page;
import africa.sendbyte.SendByteClient;
import africa.sendbyte.spi.ClientRequest;
import africa.sendbyte.spi.ClientRuntime;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/** Sending-domain operations. */
public final class Domains {
    private final ClientRuntime runtime;

    public Domains(final SendByteClient client) {
        runtime = Objects.requireNonNull(client, "client").runtime();
    }

    public Domain create(final CreateDomainRequest request) {
        Objects.requireNonNull(request, "request");

        return runtime.execute(createRequest(request), DomainApiResponse.class).toDomain();
    }

    public CompletableFuture<Domain> createAsync(final CreateDomainRequest request) {
        Objects.requireNonNull(request, "request");

        return runtime.executeAsync(createRequest(request), DomainApiResponse.class)
                .thenApply(DomainApiResponse::toDomain);
    }

    public Page<Domain> list() {
        return runtime.execute(listRequest(), DomainPageApiResponse.class).toPage();
    }

    public CompletableFuture<Page<Domain>> listAsync() {
        return runtime.executeAsync(listRequest(), DomainPageApiResponse.class)
                .thenApply(DomainPageApiResponse::toPage);
    }

    public Domain get(final String id) {
        return runtime.execute(itemRequest("domain.get", "GET", id), DomainApiResponse.class)
                .toDomain();
    }

    public CompletableFuture<Domain> getAsync(final String id) {
        return runtime.executeAsync(itemRequest("domain.get", "GET", id), DomainApiResponse.class)
                .thenApply(DomainApiResponse::toDomain);
    }

    public void delete(final String id) {
        runtime.execute(itemRequest("domain.delete", "DELETE", id), Void.class);
    }

    public CompletableFuture<Void> deleteAsync(final String id) {
        return runtime.executeAsync(itemRequest("domain.delete", "DELETE", id), Void.class);
    }

    public DomainVerification verify(final String id) {
        return runtime.execute(verifyRequest(id), DomainVerificationApiResponse.class)
                .toVerification();
    }

    public CompletableFuture<DomainVerification> verifyAsync(final String id) {
        return runtime.executeAsync(verifyRequest(id), DomainVerificationApiResponse.class)
                .thenApply(DomainVerificationApiResponse::toVerification);
    }

    public Domain setTracking(final String id, final boolean enabled) {
        return runtime.execute(trackingRequest(id, enabled), DomainApiResponse.class).toDomain();
    }

    public CompletableFuture<Domain> setTrackingAsync(final String id, final boolean enabled) {
        return runtime.executeAsync(trackingRequest(id, enabled), DomainApiResponse.class)
                .thenApply(DomainApiResponse::toDomain);
    }

    private static ClientRequest createRequest(final CreateDomainRequest request) {
        return ClientRequest.builder("domain.create", "POST")
                .pathSegment("v1")
                .pathSegment("domains")
                .body(CreateDomainApiRequest.from(request))
                .build();
    }

    private static ClientRequest listRequest() {
        return ClientRequest.builder("domain.list", "GET")
                .pathSegment("v1")
                .pathSegment("domains")
                .build();
    }

    private static ClientRequest itemRequest(
            final String operation, final String method, final String id) {
        return ClientRequest.builder(operation, method)
                .pathSegment("v1")
                .pathSegment("domains")
                .pathSegment(Emails.requireId(id, "id"))
                .build();
    }

    private static ClientRequest verifyRequest(final String id) {
        return ClientRequest.builder("domain.verify", "POST")
                .pathSegment("v1")
                .pathSegment("domains")
                .pathSegment(Emails.requireId(id, "id"))
                .pathSegment("verify")
                .build();
    }

    private static ClientRequest trackingRequest(final String id, final boolean enabled) {
        return ClientRequest.builder("domain.tracking", "POST")
                .pathSegment("v1")
                .pathSegment("domains")
                .pathSegment(Emails.requireId(id, "id"))
                .pathSegment("tracking")
                .body(new DomainTrackingApiRequest(enabled))
                .build();
    }
}

package africa.sendbyte.webhooks;

import africa.sendbyte.Page;
import africa.sendbyte.SendByteClient;
import africa.sendbyte.internal.AsyncSupport;
import africa.sendbyte.internal.WireSupport;
import africa.sendbyte.spi.ClientRequest;
import africa.sendbyte.spi.ClientRuntime;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/** Webhook endpoint and delivery operations. */
public final class Webhooks {
    private final ClientRuntime runtime;

    public Webhooks(final SendByteClient client) {
        runtime = Objects.requireNonNull(client, "client").runtime();
    }

    public WebhookEndpoint create(final CreateWebhookRequest request) {
        Objects.requireNonNull(request, "request");

        return runtime.execute(
                        collectionRequest(
                                "webhook.create", "POST", WebhookCreateApiRequest.from(request)),
                        WebhookEndpointApiResponse.class)
                .toWebhookEndpoint();
    }

    public CompletableFuture<WebhookEndpoint> createAsync(final CreateWebhookRequest request) {
        Objects.requireNonNull(request, "request");

        return AsyncSupport.map(
                runtime.executeAsync(
                        collectionRequest(
                                "webhook.create", "POST", WebhookCreateApiRequest.from(request)),
                        WebhookEndpointApiResponse.class),
                WebhookEndpointApiResponse::toWebhookEndpoint);
    }

    public Page<WebhookEndpoint> list() {
        return runtime.execute(
                        collectionRequest("webhook.list", "GET", null),
                        WebhookEndpointPageApiResponse.class)
                .toPage();
    }

    public CompletableFuture<Page<WebhookEndpoint>> listAsync() {
        return AsyncSupport.map(
                runtime.executeAsync(
                        collectionRequest("webhook.list", "GET", null),
                        WebhookEndpointPageApiResponse.class),
                WebhookEndpointPageApiResponse::toPage);
    }

    public WebhookEndpoint disable(final String id) {
        return runtime.execute(
                        itemRequest("webhook.disable", "DELETE", id, null),
                        WebhookEndpointApiResponse.class)
                .toWebhookEndpoint();
    }

    public CompletableFuture<WebhookEndpoint> disableAsync(final String id) {
        return AsyncSupport.map(
                runtime.executeAsync(
                        itemRequest("webhook.disable", "DELETE", id, null),
                        WebhookEndpointApiResponse.class),
                WebhookEndpointApiResponse::toWebhookEndpoint);
    }

    public WebhookEndpoint enable(final String id) {
        return runtime.execute(enableRequest(id), WebhookEndpointApiResponse.class)
                .toWebhookEndpoint();
    }

    public CompletableFuture<WebhookEndpoint> enableAsync(final String id) {
        return AsyncSupport.map(
                runtime.executeAsync(enableRequest(id), WebhookEndpointApiResponse.class),
                WebhookEndpointApiResponse::toWebhookEndpoint);
    }

    public WebhookEndpoint rotateSecret(final String id) {
        return runtime.execute(rotateSecretRequest(id), WebhookEndpointApiResponse.class)
                .toWebhookEndpoint();
    }

    public CompletableFuture<WebhookEndpoint> rotateSecretAsync(final String id) {
        return AsyncSupport.map(
                runtime.executeAsync(rotateSecretRequest(id), WebhookEndpointApiResponse.class),
                WebhookEndpointApiResponse::toWebhookEndpoint);
    }

    public Page<WebhookDelivery> listDeliveries(final String id) {
        return runtime.execute(deliveriesRequest(id), WebhookDeliveryPageApiResponse.class)
                .toPage();
    }

    public CompletableFuture<Page<WebhookDelivery>> listDeliveriesAsync(final String id) {
        return AsyncSupport.map(
                runtime.executeAsync(deliveriesRequest(id), WebhookDeliveryPageApiResponse.class),
                WebhookDeliveryPageApiResponse::toPage);
    }

    public WebhookReplay replayDelivery(final String id) {
        return runtime.execute(replayRequest(id), WebhookReplayApiResponse.class).toWebhookReplay();
    }

    public CompletableFuture<WebhookReplay> replayDeliveryAsync(final String id) {
        return AsyncSupport.map(
                runtime.executeAsync(replayRequest(id), WebhookReplayApiResponse.class),
                WebhookReplayApiResponse::toWebhookReplay);
    }

    private static ClientRequest collectionRequest(
            final String operation, final String method, final Object body) {
        final ClientRequest.Builder builder =
                ClientRequest.builder(operation, method).pathSegment("v1").pathSegment("webhooks");
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
                        .pathSegment("webhooks")
                        .pathSegment(WireSupport.requireText(id, "webhook endpoint id"));
        if (body != null) {
            builder.body(body);
        }
        return builder.build();
    }

    private static ClientRequest enableRequest(final String id) {
        return ClientRequest.builder("webhook.enable", "POST")
                .pathSegment("v1")
                .pathSegment("webhooks")
                .pathSegment(WireSupport.requireText(id, "webhook endpoint id"))
                .pathSegment("enable")
                .build();
    }

    private static ClientRequest rotateSecretRequest(final String id) {
        return ClientRequest.builder("webhook.rotate_secret", "POST")
                .pathSegment("v1")
                .pathSegment("webhooks")
                .pathSegment(WireSupport.requireText(id, "webhook endpoint id"))
                .pathSegment("rotate-secret")
                .build();
    }

    private static ClientRequest deliveriesRequest(final String id) {
        return ClientRequest.builder("webhook.deliveries.list", "GET")
                .pathSegment("v1")
                .pathSegment("webhooks")
                .pathSegment(WireSupport.requireText(id, "webhook endpoint id"))
                .pathSegment("deliveries")
                .build();
    }

    private static ClientRequest replayRequest(final String id) {
        return ClientRequest.builder("webhook.delivery.replay", "POST")
                .pathSegment("v1")
                .pathSegment("webhooks")
                .pathSegment("deliveries")
                .pathSegment(WireSupport.requireText(id, "webhook delivery id"))
                .pathSegment("replay")
                .idempotencyProtected(true)
                .build();
    }
}

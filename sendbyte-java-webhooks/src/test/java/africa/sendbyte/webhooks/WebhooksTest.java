package africa.sendbyte.webhooks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import africa.sendbyte.Page;
import africa.sendbyte.ResourceOwnership;
import africa.sendbyte.SendByteClient;
import africa.sendbyte.transport.HttpTransport;
import africa.sendbyte.transport.TransportRequest;
import africa.sendbyte.transport.TransportResponse;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;

final class WebhooksTest {
    @Test
    void coversEndpointAndDeliveryContractOperations() {
        final SequenceTransport transport =
                new SequenceTransport(
                        stub(
                                201,
                                endpointJson(
                                        "wh_1", "[\"email.sent\",\"future.event\"]", "whsec_once")),
                        stub(200, "{\"data\":[" + endpointJson("wh_2", "\"all\"", null) + "]}"),
                        stub(200, endpointJson("wh_1", "[\"email.delivered\"]", null)),
                        stub(200, endpointJson("wh_1", "\"all\"", null)),
                        stub(200, endpointJson("wh_1", "\"all\"", "whsec_rotated")),
                        stub(
                                200,
                                "{\"data\":[{\"id\":\"del_1\",\"event_type\":\"future.event\","
                                        + "\"status\":\"failed\",\"attempts\":2,"
                                        + "\"delivered_at\":null,"
                                        + "\"created_at\":\"2026-09-23T07:00:00Z\"}]}"),
                        stub(
                                201,
                                "{\"id\":\"del_2\",\"replay_of\":\"del_1\",\"status\":\"queued\"}"));
        final Webhooks webhooks = new Webhooks(client(transport));

        final WebhookEndpoint created =
                webhooks.create(
                        CreateWebhookRequest.builder()
                                .url("https://example.com/hooks/sendbyte")
                                .event(WebhookEventType.EMAIL_SENT)
                                .build());
        final Page<WebhookEndpoint> listed = webhooks.list();
        final WebhookEndpoint disabled = webhooks.disable("wh/id");
        final WebhookEndpoint enabled = webhooks.enable("wh/id");
        final WebhookEndpoint rotated = webhooks.rotateSecret("wh/id");
        final Page<WebhookDelivery> deliveries = webhooks.listDeliveries("wh/id");
        final WebhookReplay replay = webhooks.replayDelivery("del/id");

        assertEquals("POST", transport.requests.get(0).method());
        assertEquals("https://api.test/v1/webhooks", transport.requests.get(0).uri().toString());
        assertTrue(json(transport.requests.get(0)).contains("\"events\":[\"email.sent\"]"));
        assertEquals(WebhookEventType.EMAIL_SENT, created.events().get(0));
        assertEquals(WebhookEventType.UNKNOWN, created.events().get(1));
        assertEquals("future.event", created.rawEvents().get(1));
        assertEquals("whsec_once", created.secret());
        assertFalse(created.toString().contains("whsec_once"), created.toString());
        assertTrue(listed.data().get(0).subscribesToAllEvents());
        assertEquals(
                "https://api.test/v1/webhooks/wh%2Fid", transport.requests.get(2).uri().toString());
        assertEquals("DELETE", transport.requests.get(2).method());
        assertFalse(disabled.subscribesToAllEvents());
        assertTrue(enabled.subscribesToAllEvents());
        assertEquals(
                "https://api.test/v1/webhooks/wh%2Fid/rotate-secret",
                transport.requests.get(4).uri().toString());
        assertEquals("whsec_rotated", rotated.secret());
        assertEquals(WebhookEventType.UNKNOWN, deliveries.data().get(0).eventType());
        assertEquals("future.event", deliveries.data().get(0).rawEventType());
        assertEquals(
                "https://api.test/v1/webhooks/deliveries/del%2Fid/replay",
                transport.requests.get(6).uri().toString());
        assertEquals("del_1", replay.replayOf());
    }

    @Test
    void omitsEventsForAllSubscriptionsAndRejectsBlankValues() {
        final SequenceTransport transport =
                new SequenceTransport(stub(201, endpointJson("wh_1", "\"all\"", null)));
        final Webhooks webhooks = new Webhooks(client(transport));

        final WebhookEndpoint endpoint =
                webhooks.create(
                        CreateWebhookRequest.builder()
                                .url("https://example.com/hooks/sendbyte")
                                .build());

        assertTrue(endpoint.subscribesToAllEvents());
        assertFalse(json(transport.requests.get(0)).contains("events"));
        final IllegalArgumentException blankUrl =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> CreateWebhookRequest.builder().url(" ").build());
        final IllegalArgumentException unknownEvent =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                CreateWebhookRequest.builder()
                                        .url("https://example.com/hooks/sendbyte")
                                        .event(WebhookEventType.UNKNOWN)
                                        .build());
        final IllegalArgumentException blankId =
                assertThrows(IllegalArgumentException.class, () -> webhooks.enable(" "));

        assertEquals("webhook endpoint URL must not be blank", blankUrl.getMessage());
        assertEquals(
                "webhook event subscription must be a known contract event type",
                unknownEvent.getMessage());
        assertEquals("webhook endpoint id must not be blank", blankId.getMessage());
    }

    private static SendByteClient client(final HttpTransport transport) {
        return SendByteClient.builder()
                .apiKey("sk_test_example")
                .baseUri(URI.create("https://api.test"))
                .transport(transport, ResourceOwnership.CALLER)
                .build();
    }

    private static String endpointJson(final String id, final String events, final String secret) {
        return "{\"id\":\""
                + id
                + "\",\"url\":\"https://example.com/hooks/sendbyte\","
                + "\"events\":"
                + events
                + ",\"disabled\":false,\"disabled_at\":null,"
                + "\"consecutive_failures\":0,"
                + "\"created_at\":\"2026-09-23T07:00:00Z\""
                + (secret == null ? "" : ",\"secret\":\"" + secret + "\"")
                + "}";
    }

    private static String json(final TransportRequest request) {
        return new String(request.body(), StandardCharsets.UTF_8);
    }

    private static Stub stub(final int status, final String response) {
        return new Stub(status, response);
    }

    private static final class SequenceTransport implements HttpTransport {
        private final Queue<Stub> stubs = new ArrayDeque<>();
        private final List<TransportRequest> requests = new ArrayList<>();

        SequenceTransport(final Stub... stubs) {
            Collections.addAll(this.stubs, stubs);
        }

        @Override
        public TransportResponse execute(final TransportRequest request) {
            this.requests.add(request);
            final Stub stub = this.stubs.remove();
            return new TransportResponse(stub.status, Collections.emptyMap(), stub.response);
        }

        @Override
        public CompletableFuture<TransportResponse> executeAsync(final TransportRequest request) {
            return CompletableFuture.completedFuture(this.execute(request));
        }
    }

    private static final class Stub {
        private final int status;
        private final byte[] response;

        Stub(final int status, final String response) {
            this.status = status;
            this.response = response.getBytes(StandardCharsets.UTF_8);
        }
    }
}

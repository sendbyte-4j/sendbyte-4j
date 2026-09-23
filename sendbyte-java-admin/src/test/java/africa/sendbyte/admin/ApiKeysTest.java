package africa.sendbyte.admin;

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

final class ApiKeysTest {
    @Test
    void coversCreateListRenameAndRevokeContractOperations() {
        final SequenceTransport transport =
                new SequenceTransport(
                        stub(
                                201,
                                "{\"id\":\"key_1\",\"name\":\"Sender\",\"prefix\":\"sk_test\","
                                        + "\"last4\":\"1234\",\"scope\":\"send_only\","
                                        + "\"mode\":\"test\",\"revoked\":false,"
                                        + "\"key\":\"sk_test_secret\","
                                        + "\"created_at\":\"2026-09-09T09:15:30Z\"}"),
                        stub(
                                200,
                                "{\"data\":[{\"id\":\"key_2\",\"name\":\"Reader\","
                                        + "\"scope\":\"future_scope\",\"mode\":\"future_mode\","
                                        + "\"revoked\":false,"
                                        + "\"created_at\":\"2026-09-09T09:15:30Z\"}]}"),
                        stub(
                                200,
                                "{\"id\":\"key_1\",\"name\":\"Renamed\",\"scope\":\"full_access\","
                                        + "\"mode\":\"live\",\"revoked\":false,"
                                        + "\"created_at\":\"2026-09-09T09:15:30Z\"}"),
                        stub(
                                200,
                                "{\"id\":\"key_1\",\"name\":\"Renamed\",\"scope\":\"full_access\","
                                        + "\"mode\":\"live\",\"revoked\":true,"
                                        + "\"created_at\":\"2026-09-09T09:15:30Z\"}"));
        final ApiKeys apiKeys = new ApiKeys(client(transport));

        final ApiKey created =
                apiKeys.create(
                        CreateApiKeyRequest.builder()
                                .name("Sender")
                                .scope(ApiKeyScope.SEND_ONLY)
                                .mode(ApiKeyMode.TEST)
                                .build());
        final Page<ApiKey> listed = apiKeys.list();
        final ApiKey renamed = apiKeys.rename("key/id", "Renamed");
        final ApiKey revoked = apiKeys.revoke("key/id");

        assertEquals("POST", transport.requests.get(0).method());
        assertEquals("https://api.test/v1/api-keys", transport.requests.get(0).uri().toString());
        assertTrue(json(transport.requests.get(0)).contains("\"scope\":\"send_only\""));
        assertEquals("sk_test_secret", created.key());
        assertFalse(created.toString().contains("sk_test_secret"), created.toString());
        assertEquals(ApiKeyScope.UNKNOWN, listed.data().get(0).scope());
        assertEquals("future_scope", listed.data().get(0).rawScope());
        assertEquals(ApiKeyMode.UNKNOWN, listed.data().get(0).mode());
        assertEquals(
                "https://api.test/v1/api-keys/key%2Fid",
                transport.requests.get(2).uri().toString());
        assertEquals("PATCH", transport.requests.get(2).method());
        assertTrue(json(transport.requests.get(2)).contains("\"name\":\"Renamed\""));
        assertEquals(ApiKeyScope.FULL_ACCESS, renamed.scope());
        assertTrue(revoked.revoked());
        assertEquals("DELETE", transport.requests.get(3).method());
    }

    @Test
    void rejectsUnknownScopeModeAndBlankIdsBeforeTransport() {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        CreateApiKeyRequest.builder()
                                .name("Future")
                                .scope(ApiKeyScope.UNKNOWN)
                                .mode(ApiKeyMode.TEST)
                                .build());
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        CreateApiKeyRequest.builder()
                                .name("Future")
                                .scope(ApiKeyScope.SEND_ONLY)
                                .mode(ApiKeyMode.UNKNOWN)
                                .build());
        assertThrows(
                IllegalArgumentException.class,
                () -> new ApiKeys(client(new SequenceTransport())).revoke(" "));
    }

    private static SendByteClient client(final HttpTransport transport) {
        return SendByteClient.builder()
                .apiKey("sk_test_example")
                .baseUri(URI.create("https://api.test"))
                .transport(transport, ResourceOwnership.CALLER)
                .build();
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

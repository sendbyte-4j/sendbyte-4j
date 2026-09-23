package africa.sendbyte.marketing;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;

final class MarketingTest {
    @Test
    void coversBrandListSubscriberAndImportContractOperations() {
        final SequenceTransport transport =
                new SequenceTransport(
                        stub(200, "{\"data\":[" + brandJson("brand_1") + "],\"cap\":3,\"used\":1}"),
                        stub(201, brandJson("brand_2")),
                        stub(200, "{\"data\":[" + listJson("list_1", "future") + "]}"),
                        stub(201, listJson("list_2", "double")),
                        stub(
                                200,
                                "{\"data\":["
                                        + subscriberJson("sub_1", "future", "migration")
                                        + "],\"total\":1,\"cap\":2000,\"used\":3}"),
                        stub(
                                200,
                                "{\"id\":\"sub_2\",\"email\":\"ada@example.com\","
                                        + "\"status\":\"subscribed\",\"skipped_reason\":\"brand_opt_out\"}"),
                        stub(
                                200,
                                "{\"imported\":1,\"updated\":2,\"invalid\":1,\"skipped\":0,"
                                        + "\"overCap\":0,\"cap\":2000,"
                                        + "\"rejected\":[{\"email\":\"bad\",\"reason\":\"invalid_email\"}]}"));
        final Marketing marketing = new Marketing(client(transport));
        final Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("city", "Abuja");

        final BrandPage brands = marketing.listBrands();
        final Brand createdBrand =
                marketing.createBrand(
                        CreateBrandRequest.builder()
                                .name("Afripad")
                                .publicName("Afripad Weekly")
                                .fromAddress("hello@example.com")
                                .postalAddress("1 Market Street")
                                .build());
        final Page<SubscriberList> lists = marketing.listSubscriberLists("brand/id");
        final SubscriberList createdList =
                marketing.createSubscriberList(
                        "brand/id",
                        CreateSubscriberListRequest.builder()
                                .name("Newsletter")
                                .optIn(SubscriberListOptIn.DOUBLE)
                                .build());
        final SubscriberPage subscribers =
                marketing.listSubscribers(
                        "brand/id",
                        "list/id",
                        ListSubscribersRequest.builder()
                                .query("Ada Lagos")
                                .status(SubscriberStatus.SUBSCRIBED)
                                .limit(25)
                                .offset(50)
                                .build());
        final AddSubscriberResult added =
                marketing.addSubscriber(
                        "brand/id",
                        "list/id",
                        AddSubscriberRequest.builder()
                                .email("ada@example.com")
                                .name("Ada")
                                .attributes(attributes)
                                .build());
        final ImportSummary imported =
                marketing.importSubscribers(
                        "brand/id",
                        "list/id",
                        ImportSubscribersRequest.builder()
                                .subscriber(
                                        SubscriberImport.builder()
                                                .email("ada@example.com")
                                                .name("Ada")
                                                .attributes(attributes)
                                                .build())
                                .build());

        assertEquals("https://api.test/v1/brands", transport.requests.get(0).uri().toString());
        assertEquals(3, brands.cap());
        assertEquals("Afripad", brands.data().get(0).name());
        assertTrue(json(transport.requests.get(1)).contains("\"public_name\":\"Afripad Weekly\""));
        assertEquals("brand_2", createdBrand.id());
        assertEquals(
                "https://api.test/v1/brands/brand%2Fid/lists",
                transport.requests.get(2).uri().toString());
        assertEquals(SubscriberListOptIn.UNKNOWN, lists.data().get(0).optIn());
        assertEquals("future", lists.data().get(0).rawOptIn());
        assertTrue(json(transport.requests.get(3)).contains("\"opt_in\":\"double\""));
        assertEquals(SubscriberListOptIn.DOUBLE, createdList.optIn());
        assertEquals(
                "https://api.test/v1/brands/brand%2Fid/lists/list%2Fid/subscribers?q=Ada%20Lagos&status=subscribed&limit=25&offset=50",
                transport.requests.get(4).uri().toString());
        assertEquals(SubscriberStatus.UNKNOWN, subscribers.data().get(0).status());
        assertEquals("future", subscribers.data().get(0).rawStatus());
        assertEquals(SubscriberSource.UNKNOWN, subscribers.data().get(0).source());
        assertEquals(2000, subscribers.cap());
        assertTrue(json(transport.requests.get(5)).contains("\"attributes\":{\"city\":\"Abuja\"}"));
        assertEquals(SkippedReason.BRAND_OPT_OUT, added.skippedReason());
        assertEquals(
                "https://api.test/v1/brands/brand%2Fid/lists/list%2Fid/import",
                transport.requests.get(6).uri().toString());
        assertTrue(json(transport.requests.get(6)).contains("\"subscribers\""));
        assertEquals(1, imported.imported());
        assertEquals("invalid_email", imported.rejected().get(0).reason());
    }

    @Test
    void validatesRequiredFieldsAndProtectsRequestCollections() {
        final Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("city", "Abuja");
        final AddSubscriberRequest request =
                AddSubscriberRequest.builder()
                        .email("ada@example.com")
                        .attributes(attributes)
                        .build();
        attributes.put("city", "Lagos");

        assertEquals("Abuja", request.attributes().get("city"));
        assertThrows(UnsupportedOperationException.class, () -> request.attributes().put("x", "y"));
        assertThrows(
                IllegalArgumentException.class,
                () -> CreateBrandRequest.builder().name(" ").build());
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        CreateSubscriberListRequest.builder()
                                .name("Newsletter")
                                .optIn(SubscriberListOptIn.UNKNOWN)
                                .build());
        assertThrows(
                IllegalArgumentException.class,
                () -> ListSubscribersRequest.builder().status(SubscriberStatus.UNKNOWN).build());
        assertThrows(
                IllegalArgumentException.class,
                () -> ListSubscribersRequest.builder().limit(101).build());
        assertThrows(
                IllegalArgumentException.class,
                () -> AddSubscriberRequest.builder().email("").build());
        assertThrows(
                IllegalArgumentException.class, () -> ImportSubscribersRequest.builder().build());
        assertThrows(
                IllegalArgumentException.class,
                () -> new Marketing(client(new SequenceTransport())).listSubscriberLists(" "));
    }

    private static SendByteClient client(final HttpTransport transport) {
        return SendByteClient.builder()
                .apiKey("sk_test_example")
                .baseUri(URI.create("https://api.test"))
                .transport(transport, ResourceOwnership.CALLER)
                .build();
    }

    private static String brandJson(final String id) {
        return "{\"id\":\""
                + id
                + "\",\"name\":\"Afripad\",\"public_name\":\"Afripad Weekly\","
                + "\"logo_url\":null,\"from_address\":\"hello@example.com\","
                + "\"postal_address\":\"1 Market Street\",\"timezone\":\"Africa/Lagos\","
                + "\"created_at\":\"2026-09-23T07:00:00Z\"}";
    }

    private static String listJson(final String id, final String optIn) {
        return "{\"id\":\""
                + id
                + "\",\"name\":\"Newsletter\",\"opt_in\":\""
                + optIn
                + "\",\"counts\":{\"subscribed\":2,\"unconfirmed\":1,"
                + "\"unsubscribed\":0,\"bounced\":0,\"complained\":0},"
                + "\"created_at\":\"2026-09-23T07:00:00Z\"}";
    }

    private static String subscriberJson(
            final String id, final String status, final String source) {
        return "{\"id\":\""
                + id
                + "\",\"email\":\"ada@example.com\",\"name\":\"Ada\","
                + "\"attributes\":{\"city\":\"Abuja\"},\"status\":\""
                + status
                + "\",\"source\":\""
                + source
                + "\",\"created_at\":\"2026-09-23T07:00:00Z\"}";
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

package africa.sendbyte.marketing;

import africa.sendbyte.Page;
import africa.sendbyte.SendByteClient;
import africa.sendbyte.spi.ClientRequest;
import africa.sendbyte.spi.ClientRuntime;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/** Marketing brand, list, subscriber, and import operations. */
public final class Marketing {
    private final ClientRuntime runtime;

    public Marketing(final SendByteClient client) {
        runtime = Objects.requireNonNull(client, "client").runtime();
    }

    public BrandPage listBrands() {
        return runtime.execute(
                        brandsRequest("marketing.brands.list", "GET", null),
                        BrandPageApiResponse.class)
                .toBrandPage();
    }

    public CompletableFuture<BrandPage> listBrandsAsync() {
        return runtime.executeAsync(
                        brandsRequest("marketing.brands.list", "GET", null),
                        BrandPageApiResponse.class)
                .thenApply(BrandPageApiResponse::toBrandPage);
    }

    public Brand createBrand(final CreateBrandRequest request) {
        Objects.requireNonNull(request, "request");

        return runtime.execute(
                        brandsRequest(
                                "marketing.brands.create", "POST", BrandApiRequest.from(request)),
                        BrandApiResponse.class)
                .toBrand();
    }

    public CompletableFuture<Brand> createBrandAsync(final CreateBrandRequest request) {
        Objects.requireNonNull(request, "request");

        return runtime.executeAsync(
                        brandsRequest(
                                "marketing.brands.create", "POST", BrandApiRequest.from(request)),
                        BrandApiResponse.class)
                .thenApply(BrandApiResponse::toBrand);
    }

    public Page<SubscriberList> listSubscriberLists(final String brandId) {
        return runtime.execute(
                        listsRequest("marketing.lists.list", "GET", brandId, null),
                        SubscriberListPageApiResponse.class)
                .toPage();
    }

    public CompletableFuture<Page<SubscriberList>> listSubscriberListsAsync(final String brandId) {
        return runtime.executeAsync(
                        listsRequest("marketing.lists.list", "GET", brandId, null),
                        SubscriberListPageApiResponse.class)
                .thenApply(SubscriberListPageApiResponse::toPage);
    }

    public SubscriberList createSubscriberList(
            final String brandId, final CreateSubscriberListRequest request) {
        Objects.requireNonNull(request, "request");

        return runtime.execute(
                        listsRequest(
                                "marketing.lists.create",
                                "POST",
                                brandId,
                                SubscriberListApiRequest.from(request)),
                        SubscriberListApiResponse.class)
                .toSubscriberList();
    }

    public CompletableFuture<SubscriberList> createSubscriberListAsync(
            final String brandId, final CreateSubscriberListRequest request) {
        Objects.requireNonNull(request, "request");

        return runtime.executeAsync(
                        listsRequest(
                                "marketing.lists.create",
                                "POST",
                                brandId,
                                SubscriberListApiRequest.from(request)),
                        SubscriberListApiResponse.class)
                .thenApply(SubscriberListApiResponse::toSubscriberList);
    }

    public SubscriberPage listSubscribers(
            final String brandId, final String listId, final ListSubscribersRequest request) {
        Objects.requireNonNull(request, "request");

        return runtime.execute(
                        subscribersRequest(
                                "marketing.subscribers.list",
                                "GET",
                                brandId,
                                listId,
                                request,
                                null),
                        SubscriberPageApiResponse.class)
                .toSubscriberPage();
    }

    public CompletableFuture<SubscriberPage> listSubscribersAsync(
            final String brandId, final String listId, final ListSubscribersRequest request) {
        Objects.requireNonNull(request, "request");

        return runtime.executeAsync(
                        subscribersRequest(
                                "marketing.subscribers.list",
                                "GET",
                                brandId,
                                listId,
                                request,
                                null),
                        SubscriberPageApiResponse.class)
                .thenApply(SubscriberPageApiResponse::toSubscriberPage);
    }

    public AddSubscriberResult addSubscriber(
            final String brandId, final String listId, final AddSubscriberRequest request) {
        Objects.requireNonNull(request, "request");

        return runtime.execute(
                        subscribersRequest(
                                "marketing.subscribers.add",
                                "POST",
                                brandId,
                                listId,
                                null,
                                SubscriberApiRequest.from(request)),
                        AddSubscriberResultApiResponse.class)
                .toAddSubscriberResult();
    }

    public CompletableFuture<AddSubscriberResult> addSubscriberAsync(
            final String brandId, final String listId, final AddSubscriberRequest request) {
        Objects.requireNonNull(request, "request");

        return runtime.executeAsync(
                        subscribersRequest(
                                "marketing.subscribers.add",
                                "POST",
                                brandId,
                                listId,
                                null,
                                SubscriberApiRequest.from(request)),
                        AddSubscriberResultApiResponse.class)
                .thenApply(AddSubscriberResultApiResponse::toAddSubscriberResult);
    }

    public ImportSummary importSubscribers(
            final String brandId, final String listId, final ImportSubscribersRequest request) {
        Objects.requireNonNull(request, "request");

        return runtime.execute(
                        importRequest(brandId, listId, ImportSubscribersApiRequest.from(request)),
                        ImportSummaryApiResponse.class)
                .toImportSummary();
    }

    public CompletableFuture<ImportSummary> importSubscribersAsync(
            final String brandId, final String listId, final ImportSubscribersRequest request) {
        Objects.requireNonNull(request, "request");

        return runtime.executeAsync(
                        importRequest(brandId, listId, ImportSubscribersApiRequest.from(request)),
                        ImportSummaryApiResponse.class)
                .thenApply(ImportSummaryApiResponse::toImportSummary);
    }

    private static ClientRequest brandsRequest(
            final String operation, final String method, final Object body) {
        final ClientRequest.Builder builder =
                ClientRequest.builder(operation, method).pathSegment("v1").pathSegment("brands");
        if (body != null) {
            builder.body(body);
        }
        return builder.build();
    }

    private static ClientRequest listsRequest(
            final String operation, final String method, final String brandId, final Object body) {
        final ClientRequest.Builder builder =
                ClientRequest.builder(operation, method)
                        .pathSegment("v1")
                        .pathSegment("brands")
                        .pathSegment(WireSupport.requireText(brandId, "brand id"))
                        .pathSegment("lists");
        if (body != null) {
            builder.body(body);
        }
        return builder.build();
    }

    private static ClientRequest subscribersRequest(
            final String operation,
            final String method,
            final String brandId,
            final String listId,
            final ListSubscribersRequest query,
            final Object body) {
        final ClientRequest.Builder builder =
                ClientRequest.builder(operation, method)
                        .pathSegment("v1")
                        .pathSegment("brands")
                        .pathSegment(WireSupport.requireText(brandId, "brand id"))
                        .pathSegment("lists")
                        .pathSegment(WireSupport.requireText(listId, "subscriber list id"))
                        .pathSegment("subscribers");
        if (query != null) {
            applySubscriberQuery(builder, query);
        }
        if (body != null) {
            builder.body(body);
        }
        return builder.build();
    }

    private static ClientRequest importRequest(
            final String brandId, final String listId, final Object body) {
        return ClientRequest.builder("marketing.subscribers.import", "POST")
                .pathSegment("v1")
                .pathSegment("brands")
                .pathSegment(WireSupport.requireText(brandId, "brand id"))
                .pathSegment("lists")
                .pathSegment(WireSupport.requireText(listId, "subscriber list id"))
                .pathSegment("import")
                .body(body)
                .build();
    }

    private static void applySubscriberQuery(
            final ClientRequest.Builder builder, final ListSubscribersRequest request) {
        if (request.query() != null) {
            builder.queryParameter("q", request.query());
        }
        if (request.status() != null) {
            builder.queryParameter("status", request.status().wireValue());
        }
        if (request.limit() != null) {
            builder.queryParameter("limit", request.limit().toString());
        }
        if (request.offset() != null) {
            builder.queryParameter("offset", request.offset().toString());
        }
    }
}

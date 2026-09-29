package africa.sendbyte.email;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import africa.sendbyte.Page;
import africa.sendbyte.ResourceOwnership;
import africa.sendbyte.SendByteClient;
import africa.sendbyte.exception.RateLimitException;
import africa.sendbyte.exception.ValidationException;
import africa.sendbyte.transport.HttpTransport;
import africa.sendbyte.transport.TransportRequest;
import africa.sendbyte.transport.TransportResponse;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

final class EmailsTest {
    @Test
    void sendsContractRequestAndMapsImmutableResponse() {
        final CapturingTransport transport =
                new CapturingTransport(
                        201,
                        "{\"id\":\"em_123\",\"from\":\"sender@example.com\",\"to\":[\"to@example.com\"],"
                                + "\"subject\":\"Receipt\",\"status\":\"queued\",\"sandbox\":true,"
                                + "\"tags\":[\"receipt\"],\"message_id\":\"msg_123\","
                                + "\"open_count\":1,\"click_count\":0,"
                                + "\"first_opened_at\":\"2026-09-09T10:16:30Z\","
                                + "\"created_at\":\"2026-09-09T10:15:30Z\"}");
        final SendByteClient client = client(transport);
        final Emails emails = new Emails(client);

        final SendEmailRequest request =
                SendEmailRequest.builder()
                        .from("sender@example.com")
                        .to("to@example.com")
                        .replyTo("support@example.com")
                        .subject("Receipt")
                        .html("<p>Paid</p>")
                        .attachment(
                                Attachment.builder(
                                                "receipt.txt",
                                                "text/plain",
                                                "paid".getBytes(StandardCharsets.UTF_8))
                                        .build())
                        .tag("receipt")
                        .header("X-Campaign", "orders")
                        .listUnsubscribe(
                                ListUnsubscribe.of("https://example.com/unsubscribe", null))
                        .scheduledAt(OffsetDateTime.parse("2026-09-10T09:00:00+01:00"))
                        .idempotencyKey("order-123")
                        .tracking(TrackingOptions.of(true, false))
                        .build();

        final Email email = emails.send(request);

        assertEquals("POST", transport.request.method());
        assertEquals("https://api.test/v1/emails", transport.request.uri().toString());
        assertEquals("Bearer sk_test_example", first(transport.request, "Authorization"));
        assertEquals("application/json", first(transport.request, "Content-Type"));
        final String json = new String(transport.request.body(), StandardCharsets.UTF_8);
        assertTrue(json.contains("\"reply_to\":[\"support@example.com\"]"), json);
        assertTrue(json.contains("\"idempotency_key\":\"order-123\""), json);
        assertTrue(json.contains("\"content_type\":\"text/plain\""), json);
        assertTrue(json.contains("\"list_unsubscribe\""), json);
        assertTrue(json.contains("\"scheduled_at\""), json);
        assertTrue(json.contains("\"tracking\":{\"opens\":true,\"clicks\":false}"), json);
        assertFalse(json.contains("template_id"), json);
        assertFalse(json.contains("replyTo"), json);

        assertEquals("em_123", email.id());
        assertEquals(EmailStatus.QUEUED, email.status());
        assertEquals("queued", email.rawStatus());
        assertEquals("msg_123", email.messageId());
        assertEquals(1, email.openCount());
        assertTrue(email.sandbox());
        assertEquals(OffsetDateTime.parse("2026-09-09T10:16:30Z"), email.firstOpenedAt());
        assertEquals(OffsetDateTime.parse("2026-09-09T10:15:30Z"), email.createdAt());
        assertThrows(
                UnsupportedOperationException.class, () -> email.to().add("other@example.com"));
    }

    @Test
    void mapsTypedErrorsAndResponseMetadata() {
        final Map<String, List<String>> headers = new LinkedHashMap<>();
        headers.put("X-Request-Id", Collections.singletonList("req_123"));
        final CapturingTransport validation =
                new CapturingTransport(
                        422,
                        "{\"error\":{\"code\":\"validation_error\",\"message\":\"subject is invalid\","
                                + "\"docs_url\":\"https://docs.example.test/errors/validation_error\"}}",
                        headers);

        final ValidationException failure =
                assertThrows(
                        ValidationException.class,
                        () -> new Emails(client(validation)).send(minimalRequest()));

        assertEquals(422, failure.statusCode());
        assertEquals("validation_error", failure.code());
        assertEquals("req_123", failure.requestId());

        headers.put("Retry-After", Collections.singletonList("17"));
        final CapturingTransport limited =
                new CapturingTransport(
                        429,
                        "{\"error\":{\"code\":\"rate_limit_exceeded\",\"message\":\"slow down\","
                                + "\"docs_url\":\"https://docs.example.test/errors/rate_limit_exceeded\"}}",
                        headers);
        final RateLimitException rateLimit =
                assertThrows(
                        RateLimitException.class,
                        () -> new Emails(client(limited)).send(minimalRequest()));
        assertEquals(Duration.ofSeconds(17), rateLimit.retryAfter());
    }

    @Test
    void propagatesAsyncCancellationToTransport() {
        final PendingTransport transport = new PendingTransport();
        final CompletableFuture<Email> send =
                new Emails(client(transport)).sendAsync(minimalRequest());

        assertTrue(send.cancel(true));

        assertTrue(transport.pending.isCancelled());
    }

    @Test
    void propagatesAsyncCancellationFromMappedOperations() {
        final PendingTransport transport = new PendingTransport();
        final CompletableFuture<EmailDetail> get = new Emails(client(transport)).getAsync("em_1");

        assertTrue(get.cancel(true));

        assertTrue(transport.pending.isCancelled());
    }

    @Test
    void defaultJdkTransportPerformsRealLocalHttpRequest() throws Exception {
        final HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        final AtomicReference<String> authorization = new AtomicReference<>();
        final AtomicReference<String> body = new AtomicReference<>();

        server.createContext(
                "/v1/emails",
                exchange -> {
                    authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
                    body.set(
                            new String(
                                    exchange.getRequestBody().readAllBytes(),
                                    StandardCharsets.UTF_8));
                    final byte[] response =
                            "{\"id\":\"em_local\",\"status\":\"queued\",\"created_at\":\"2026-09-09T10:15:30Z\"}"
                                    .getBytes(StandardCharsets.UTF_8);

                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(201, response.length);
                    exchange.getResponseBody().write(response);
                    exchange.close();
                });
        server.start();

        try (SendByteClient client =
                SendByteClient.builder()
                        .apiKey("sk_test_local")
                        .baseUri(URI.create("http://127.0.0.1:" + server.getAddress().getPort()))
                        .build()) {
            final Email email =
                    new Emails(client).sendAsync(minimalRequest()).get(5, TimeUnit.SECONDS);

            assertEquals("em_local", email.id());
            assertEquals("Bearer sk_test_local", authorization.get());
            assertTrue(body.get().contains("\"subject\":\"Hello\""), body.get());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void validatesContractRequirementsBeforeTransport() {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        SendEmailRequest.builder()
                                .from("sender@example.com")
                                .to("to@example.com")
                                .text("body")
                                .build());
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        SendEmailRequest.builder()
                                .from("sender@example.com")
                                .to("to@example.com")
                                .subject("subject")
                                .build());
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        SendEmailRequest.builder()
                                .from("sender@example.com")
                                .to(Collections.nCopies(51, "to@example.com"))
                                .subject("subject")
                                .text("body")
                                .build());
    }

    @Test
    void retrievesListsAndExportsEmailsWithContractPathsAndMapping() {
        final Map<String, List<String>> csvHeaders = new LinkedHashMap<>();
        csvHeaders.put(
                "Content-Disposition",
                Collections.singletonList("attachment; filename=\"emails.csv\""));
        final SequenceTransport transport =
                new SequenceTransport(
                        stub(
                                200,
                                "{\"id\":\"em_1\",\"from\":\"sender@example.com\","
                                        + "\"to\":[\"to@example.com\"],\"subject\":\"Hello\","
                                        + "\"status\":\"delivered\",\"events\":[{\"type\":\"delivered\","
                                        + "\"payload\":{\"mx\":\"example\"},"
                                        + "\"created_at\":\"2026-09-09T10:18:30Z\"}],"
                                        + "\"created_at\":\"2026-09-09T10:15:30Z\"}"),
                        stub(
                                200,
                                "{\"data\":[{\"id\":\"em_2\",\"status\":\"mystery\","
                                        + "\"created_at\":\"2026-09-09T11:15:30Z\"}],"
                                        + "\"has_more\":true}"),
                        stub(200, "id,status\nem_2,delivered\n", csvHeaders));
        final Emails emails = new Emails(client(transport));

        final EmailDetail detail = emails.get("em/id 1");
        final Page<Email> page =
                emails.list(
                        ListEmailsRequest.builder()
                                .limit(2)
                                .after("cursor/1")
                                .status(EmailStatus.DELIVERED)
                                .build());
        final EmailExport export =
                emails.export(
                        ExportEmailsRequest.builder()
                                .status(EmailStatus.DELIVERED)
                                .domainId("domain/1")
                                .sandbox(true)
                                .days(30)
                                .search("hello world")
                                .build());

        assertEquals(
                "https://api.test/v1/emails/em%2Fid%201",
                transport.requests.get(0).uri().toString());
        assertEquals(EmailStatus.DELIVERED, detail.email().status());
        assertEquals("delivered", detail.events().get(0).type());
        assertEquals(
                "https://api.test/v1/emails?limit=2&after=cursor%2F1&status=delivered",
                transport.requests.get(1).uri().toString());
        assertTrue(page.hasMore());
        assertEquals(EmailStatus.UNKNOWN, page.data().get(0).status());
        assertEquals("mystery", page.data().get(0).rawStatus());
        assertEquals(
                "https://api.test/v1/emails/export?status=delivered&domain_id=domain%2F1&sandbox=true&days=30&search=hello%20world",
                transport.requests.get(2).uri().toString());
        assertEquals("attachment; filename=\"emails.csv\"", export.contentDisposition());
        assertEquals(
                "id,status\nem_2,delivered\n",
                new String(export.content(), StandardCharsets.UTF_8));
    }

    @Test
    void domainsCoverContractOperationsAndUnknownWireValues() {
        final SequenceTransport transport =
                new SequenceTransport(
                        stub(201, domainJson("dom_1", "pending", "TXT")),
                        stub(200, "{\"data\":[" + domainJson("dom_2", "verified", "CNAME") + "]}"),
                        stub(200, domainJson("dom_3", "warming", "ALIAS")),
                        stub(
                                200,
                                "{\"id\":\"dom_3\",\"domain\":\"example.com\",\"status\":\"verified\","
                                        + "\"ses_verified\":true,\"deliverability_ready\":true,"
                                        + "\"checks\":[{\"type\":\"ALIAS\",\"host\":\"@\",\"name\":\"@\","
                                        + "\"value\":\"sendbyte\",\"purpose\":\"dkim\",\"required\":true}],"
                                        + "\"tracking\":\"active\"}"),
                        stub(200, domainJson("dom_3", "verified", "TXT")),
                        stub(204, ""));
        final Domains domains = new Domains(client(transport));

        final Domain created =
                domains.create(CreateDomainRequest.builder().domain("example.com").build());
        final Page<Domain> listed = domains.list();
        final Domain fetched = domains.get("dom/id 3");
        final DomainVerification verification = domains.verify("dom/id 3");
        domains.setTracking("dom/id 3", true);
        domains.delete("dom/id 3");

        assertEquals("POST", transport.requests.get(0).method());
        assertTrue(json(transport.requests.get(0)).contains("\"domain\":\"example.com\""));
        assertEquals("https://api.test/v1/domains", transport.requests.get(1).uri().toString());
        assertEquals(
                "https://api.test/v1/domains/dom%2Fid%203",
                transport.requests.get(2).uri().toString());
        assertEquals(DomainStatus.UNKNOWN, fetched.status());
        assertEquals("warming", fetched.rawStatus());
        assertEquals(DnsRecordType.UNKNOWN, fetched.dnsRecords().get(0).type());
        assertEquals("ALIAS", fetched.dnsRecords().get(0).rawType());
        assertEquals(DomainStatus.PENDING, created.status());
        assertEquals("dom_2", listed.data().get(0).id());
        assertEquals(
                "https://api.test/v1/domains/dom%2Fid%203/verify",
                transport.requests.get(3).uri().toString());
        assertEquals(DomainTrackingStatus.ACTIVE, verification.tracking());
        assertEquals("active", verification.rawTracking());
        assertEquals("ALIAS", verification.checks().get(0).rawType());
        assertTrue(verification.sesVerified());
        assertEquals(
                "https://api.test/v1/domains/dom%2Fid%203/tracking",
                transport.requests.get(4).uri().toString());
        assertTrue(json(transport.requests.get(4)).contains("\"enabled\":true"));
        assertEquals("DELETE", transport.requests.get(5).method());
    }

    @Test
    void templatesCoverSavedAndInlineRenderingOperations() {
        final SequenceTransport transport =
                new SequenceTransport(
                        stub(201, templateJson("tpl_1", "html")),
                        stub(200, "{\"data\":[" + templateJson("tpl_1", "mjml") + "]}"),
                        stub(200, templateJson("tpl_1", "text")),
                        stub(200, templateJson("tpl_1", "future")),
                        stub(200, "{\"id\":\"tpl_1\",\"deleted\":true}"),
                        stub(
                                200,
                                "{\"subject\":\"Hi Ada\",\"html\":\"<p>Hi</p>\",\"text\":\"Hi\"}"),
                        stub(200, "{\"subject\":\"Saved Ada\",\"html\":\"<p>Saved</p>\"}"));
        final Templates templates = new Templates(client(transport));
        final TemplateInput input =
                TemplateInput.builder()
                        .name("welcome_1")
                        .subject("Welcome {{name}}")
                        .html("<p>Welcome</p>")
                        .build();

        final Template created = templates.create(input);
        final Page<Template> listed = templates.list();
        templates.get("welcome/name");
        final Template updated = templates.update("welcome/name", input);
        final TemplateDeletion deletion = templates.delete("welcome/name");
        final RenderedTemplate rendered =
                templates.render(
                        RenderTemplateRequest.builder()
                                .subject("Hi {{name}}")
                                .html("<p>Hi</p>")
                                .variable("name", "Ada")
                                .build());
        final RenderedTemplate preview =
                templates.preview(
                        "welcome/name",
                        PreviewTemplateRequest.builder().variable("name", "Ada").build());

        assertEquals(TemplateFormat.HTML, created.format());
        assertEquals(TemplateFormat.MJML, listed.data().get(0).format());
        assertEquals(TemplateFormat.UNKNOWN, updated.format());
        assertEquals("future", updated.rawFormat());
        assertTrue(deletion.deleted());
        assertEquals("Hi Ada", rendered.subject());
        assertEquals("Saved Ada", preview.subject());
        assertEquals(
                "https://api.test/v1/templates/welcome%2Fname",
                transport.requests.get(2).uri().toString());
        assertEquals("PUT", transport.requests.get(3).method());
        assertFalse(
                json(transport.requests.get(3)).contains("mjml"), json(transport.requests.get(3)));
        assertEquals(
                "https://api.test/v1/templates/render", transport.requests.get(5).uri().toString());
        assertEquals(
                "https://api.test/v1/templates/welcome%2Fname/preview",
                transport.requests.get(6).uri().toString());
    }

    @Test
    void suppressionsCoverLookupAndRemovalLifecycle() {
        final SequenceTransport transport =
                new SequenceTransport(
                        stub(
                                200,
                                "{\"data\":[{\"address\":\"bounce@example.com\",\"reason\":\"bounce\","
                                        + "\"source\":\"ses\",\"eligible\":true,"
                                        + "\"created_at\":\"2026-09-09T10:15:30Z\"}]}"),
                        stub(
                                200,
                                "{\"address\":\"bounce@example.com\",\"reason\":\"bounce\","
                                        + "\"source\":\"ses\",\"eligible\":true,"
                                        + "\"request\":{\"id\":\"rem_1\",\"status\":\"future\"},"
                                        + "\"created_at\":\"2026-09-09T10:15:30Z\"}"),
                        stub(201, removalJson("pending_verification", "api")),
                        stub(200, "{\"data\":[" + removalJson("removed", "dashboard") + "]}"),
                        stub(200, removalJson("expired", "future")),
                        stub(200, removalJson("cancelled", "api")));
        final Suppressions suppressions = new Suppressions(client(transport));

        final Page<Suppression> page =
                suppressions.list(
                        ListSuppressionsRequest.builder()
                                .search("bounce@example.com")
                                .limit(10)
                                .build());
        final Suppression suppression = suppressions.get("bounce+tag@example.com");
        final SuppressionRemoval created =
                suppressions.requestRemoval(
                        CreateSuppressionRemovalRequest.builder()
                                .address("bounce@example.com")
                                .note("fixed")
                                .build());
        final Page<SuppressionRemoval> removals = suppressions.listRemovals();
        final SuppressionRemoval fetched = suppressions.getRemoval("rem/1");
        final SuppressionRemoval cancelled = suppressions.cancelRemoval("rem/1");

        assertEquals(
                "https://api.test/v1/suppressions?search=bounce%40example.com&limit=10",
                transport.requests.get(0).uri().toString());
        assertEquals("bounce@example.com", page.data().get(0).address());
        assertEquals(
                "https://api.test/v1/suppressions/bounce%2Btag%40example.com",
                transport.requests.get(1).uri().toString());
        assertEquals(SuppressionRemovalStatus.UNKNOWN, suppression.request().status());
        assertTrue(json(transport.requests.get(2)).contains("\"note\":\"fixed\""));
        assertEquals(SuppressionRemovalStatus.PENDING_VERIFICATION, created.status());
        assertEquals(SuppressionRemovalSource.DASHBOARD, removals.data().get(0).source());
        assertEquals(SuppressionRemovalSource.UNKNOWN, fetched.source());
        assertEquals(SuppressionRemovalStatus.CANCELLED, cancelled.status());
        assertEquals(1, created.timeline().size());
        assertEquals("created", created.timeline().get(0).action());
        assertEquals("api", created.timeline().get(0).actor());
        assertEquals(OffsetDateTime.parse("2026-09-09T09:15:30Z"), created.timeline().get(0).at());
        assertEquals(Map.of("note", "fixed"), created.timeline().get(0).metadata());
        assertEquals(
                "https://api.test/v1/suppressions/removals/rem%2F1",
                transport.requests.get(4).uri().toString());
        assertEquals("DELETE", transport.requests.get(5).method());
    }

    private static SendEmailRequest minimalRequest() {
        return SendEmailRequest.builder()
                .from("sender@example.com")
                .to("to@example.com")
                .subject("Hello")
                .text("Hello")
                .build();
    }

    private static SendByteClient client(final HttpTransport transport) {
        return SendByteClient.builder()
                .apiKey("sk_test_example")
                .baseUri(URI.create("https://api.test"))
                .transport(transport, ResourceOwnership.CALLER)
                .build();
    }

    private static String first(final TransportRequest request, final String header) {
        return request.headers().entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(header))
                .flatMap(entry -> entry.getValue().stream())
                .findFirst()
                .orElse(null);
    }

    private static String json(final TransportRequest request) {
        return new String(request.body(), StandardCharsets.UTF_8);
    }

    private static Stub stub(final int status, final String response) {
        return stub(status, response, Collections.emptyMap());
    }

    private static Stub stub(
            final int status, final String response, final Map<String, List<String>> headers) {
        return new Stub(status, response, headers);
    }

    private static String domainJson(
            final String id, final String status, final String recordType) {
        return "{\"id\":\""
                + id
                + "\",\"domain\":\"example.com\",\"status\":\""
                + status
                + "\",\"dkim_selector\":\"sb1\",\"dns_records\":[{\"type\":\""
                + recordType
                + "\",\"host\":\"@\",\"name\":\"@\",\"value\":\"sendbyte\","
                + "\"purpose\":\"dkim\",\"required\":true}],"
                + "\"verified_at\":\"2026-09-09T10:15:30Z\","
                + "\"deliverability_ready\":true,"
                + "\"tracking\":{\"enabled\":false,\"host\":\"click.example.com\","
                + "\"status\":\"pending\"},"
                + "\"created_at\":\"2026-09-09T09:15:30Z\"}";
    }

    private static String templateJson(final String id, final String format) {
        return "{\"id\":\""
                + id
                + "\",\"name\":\"welcome_1\",\"subject\":\"Welcome\","
                + "\"version\":2,\"format\":\""
                + format
                + "\",\"html\":\"<p>Welcome</p>\","
                + "\"created_at\":\"2026-09-09T09:15:30Z\","
                + "\"updated_at\":\"2026-09-09T10:15:30Z\"}";
    }

    private static String removalJson(final String status, final String source) {
        return "{\"id\":\"rem_1\",\"address\":\"bounce@example.com\",\"reason\":\"bounce\","
                + "\"status\":\""
                + status
                + "\",\"note\":\"fixed\",\"source\":\""
                + source
                + "\",\"verification_sends\":1,"
                + "\"created_at\":\"2026-09-09T09:15:30Z\","
                + "\"expires_at\":\"2026-09-16T09:15:30Z\","
                + "\"timeline\":[{\"action\":\"created\",\"actor\":\"api\","
                + "\"at\":\"2026-09-09T09:15:30Z\",\"metadata\":{\"note\":\"fixed\"}}]}";
    }

    private static final class CapturingTransport implements HttpTransport {
        private final int status;
        private final byte[] response;
        private final Map<String, List<String>> headers;
        private TransportRequest request;

        CapturingTransport(final int status, final String response) {
            this(status, response, Collections.emptyMap());
        }

        CapturingTransport(
                final int status, final String response, final Map<String, List<String>> headers) {
            this.status = status;
            this.response = response.getBytes(StandardCharsets.UTF_8);
            this.headers = headers;
        }

        @Override
        public TransportResponse execute(final TransportRequest request) {
            this.request = request;
            return new TransportResponse(this.status, this.headers, this.response);
        }

        @Override
        public CompletableFuture<TransportResponse> executeAsync(final TransportRequest request) {
            this.request = request;
            return CompletableFuture.completedFuture(
                    new TransportResponse(this.status, this.headers, this.response));
        }
    }

    private static final class PendingTransport implements HttpTransport {
        private final CompletableFuture<TransportResponse> pending = new CompletableFuture<>();

        @Override
        public TransportResponse execute(final TransportRequest request) throws IOException {
            throw new IOException("not used");
        }

        @Override
        public CompletableFuture<TransportResponse> executeAsync(final TransportRequest request) {
            return this.pending;
        }
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
            return new TransportResponse(stub.status, stub.headers, stub.response);
        }

        @Override
        public CompletableFuture<TransportResponse> executeAsync(final TransportRequest request) {
            return CompletableFuture.completedFuture(this.execute(request));
        }
    }

    private static final class Stub {
        private final int status;
        private final byte[] response;
        private final Map<String, List<String>> headers;

        Stub(final int status, final String response, final Map<String, List<String>> headers) {
            this.status = status;
            this.response = response.getBytes(StandardCharsets.UTF_8);
            this.headers = headers;
        }
    }
}

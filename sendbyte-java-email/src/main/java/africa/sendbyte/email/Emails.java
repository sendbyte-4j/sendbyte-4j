package africa.sendbyte.email;

import africa.sendbyte.DetailedResponse;
import africa.sendbyte.Page;
import africa.sendbyte.SendByteClient;
import africa.sendbyte.internal.AsyncSupport;
import africa.sendbyte.spi.ClientRequest;
import africa.sendbyte.spi.ClientRuntime;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/** Transactional email operations. */
public final class Emails {
    private final ClientRuntime runtime;

    public Emails(final SendByteClient client) {
        runtime = Objects.requireNonNull(client, "client").runtime();
    }

    public Email send(final SendEmailRequest request) {
        Objects.requireNonNull(request, "request");

        return runtime.execute(sendRequest(request), EmailApiResponse.class).toEmail();
    }

    public CompletableFuture<Email> sendAsync(final SendEmailRequest request) {
        Objects.requireNonNull(request, "request");

        return AsyncSupport.map(
                runtime.executeAsync(sendRequest(request), EmailApiResponse.class),
                EmailApiResponse::toEmail);
    }

    public EmailDetail get(final String id) {
        return runtime.execute(getRequest(id), EmailApiResponse.class).toEmailDetail();
    }

    public CompletableFuture<EmailDetail> getAsync(final String id) {
        return AsyncSupport.map(
                runtime.executeAsync(getRequest(id), EmailApiResponse.class),
                EmailApiResponse::toEmailDetail);
    }

    public Page<Email> list(final ListEmailsRequest request) {
        Objects.requireNonNull(request, "request");

        return runtime.execute(listRequest(request), EmailPageApiResponse.class).toPage();
    }

    public CompletableFuture<Page<Email>> listAsync(final ListEmailsRequest request) {
        Objects.requireNonNull(request, "request");

        return AsyncSupport.map(
                runtime.executeAsync(listRequest(request), EmailPageApiResponse.class),
                EmailPageApiResponse::toPage);
    }

    public EmailExport export(final ExportEmailsRequest request) {
        Objects.requireNonNull(request, "request");

        final DetailedResponse<byte[]> response =
                runtime.executeDetailed(exportRequest(request), byte[].class);
        return new EmailExport(response.value(), response.metadata());
    }

    public CompletableFuture<EmailExport> exportAsync(final ExportEmailsRequest request) {
        Objects.requireNonNull(request, "request");

        return AsyncSupport.map(
                runtime.executeDetailedAsync(exportRequest(request), byte[].class),
                response -> new EmailExport(response.value(), response.metadata()));
    }

    private static ClientRequest sendRequest(final SendEmailRequest request) {
        return ClientRequest.builder("email.send", "POST", "/v1/emails")
                .idempotencyProtected(request.getIdempotencyKey() != null)
                .body(SendEmailApiRequest.from(request))
                .build();
    }

    private static ClientRequest getRequest(final String id) {
        return ClientRequest.builder("email.get", "GET")
                .pathSegment("v1")
                .pathSegment("emails")
                .pathSegment(requireId(id, "id"))
                .build();
    }

    private static ClientRequest listRequest(final ListEmailsRequest request) {
        final ClientRequest.Builder builder =
                ClientRequest.builder("email.list", "GET").pathSegment("v1").pathSegment("emails");
        if (request.limit() != null) {
            builder.queryParameter("limit", request.limit().toString());
        }
        if (request.after() != null) {
            builder.queryParameter("after", request.after());
        }
        if (request.status() != null && request.status() != EmailStatus.UNKNOWN) {
            builder.queryParameter("status", request.status().wireValue());
        }
        return builder.build();
    }

    private static ClientRequest exportRequest(final ExportEmailsRequest request) {
        final ClientRequest.Builder builder =
                ClientRequest.builder("email.export", "GET")
                        .pathSegment("v1")
                        .pathSegment("emails")
                        .pathSegment("export");
        if (request.status() != null && request.status() != EmailStatus.UNKNOWN) {
            builder.queryParameter("status", request.status().wireValue());
        }
        if (request.domainId() != null) {
            builder.queryParameter("domain_id", request.domainId());
        }
        if (request.sandbox() != null) {
            builder.queryParameter("sandbox", request.sandbox().toString());
        }
        if (request.days() != null) {
            builder.queryParameter("days", request.days().toString());
        }
        if (request.search() != null) {
            builder.queryParameter("search", request.search());
        }
        return builder.build();
    }

    static String requireId(final String value, final String name) {
        Objects.requireNonNull(value, name);
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}

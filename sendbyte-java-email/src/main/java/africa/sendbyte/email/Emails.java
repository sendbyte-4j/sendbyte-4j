package africa.sendbyte.email;

import africa.sendbyte.SendByteClient;
import africa.sendbyte.spi.ClientRequest;
import africa.sendbyte.spi.ClientRuntime;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/** Transactional email operations. */
public final class Emails {
    private final ClientRuntime runtime;

    public Emails(final SendByteClient client) {
        this.runtime = Objects.requireNonNull(client, "client").runtime();
    }

    public Email send(final SendEmailRequest request) {
        Objects.requireNonNull(request, "request");

        return runtime.execute(sendRequest(request), EmailApiResponse.class).toEmail();
    }

    public CompletableFuture<Email> sendAsync(final SendEmailRequest request) {
        Objects.requireNonNull(request, "request");

        final CompletableFuture<EmailApiResponse> source =
                runtime.executeAsync(sendRequest(request), EmailApiResponse.class);
        final CompletableFuture<Email> result = new CompletableFuture<>();

        source.whenComplete(
                (wire, failure) -> {
                    if (failure != null) {
                        result.completeExceptionally(failure);
                    } else {
                        try {
                            result.complete(wire.toEmail());
                        } catch (final RuntimeException mappingFailure) {
                            result.completeExceptionally(mappingFailure);
                        }
                    }
                });
        result.whenComplete(
                (ignored, failure) -> {
                    if (result.isCancelled()) {
                        source.cancel(true);
                    }
                });
        return result;
    }

    private static ClientRequest sendRequest(final SendEmailRequest request) {
        return ClientRequest.builder("email.send", "POST", "/v1/emails")
                .idempotencyProtected(request.getIdempotencyKey() != null)
                .body(SendEmailApiRequest.from(request))
                .build();
    }
}

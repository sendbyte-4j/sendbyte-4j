package africa.sendbyte.quarkus;

import africa.sendbyte.SendByteClient;
import africa.sendbyte.admin.ApiKeys;
import africa.sendbyte.email.Domains;
import africa.sendbyte.email.Emails;
import africa.sendbyte.email.Suppressions;
import africa.sendbyte.email.Templates;
import africa.sendbyte.marketing.Marketing;
import africa.sendbyte.webhooks.Webhooks;
import java.util.Objects;

/** Product facade factory used by Quarkus CDI producer wiring. */
public final class SendByteQuarkusProductFactory {
    public Emails emails(final SendByteClient client, final SendByteQuarkusConfig config) {
        requireEnabled(config.email(), "email");
        return new Emails(Objects.requireNonNull(client, "client"));
    }

    public Domains domains(final SendByteClient client, final SendByteQuarkusConfig config) {
        requireEnabled(config.email(), "email");
        return new Domains(Objects.requireNonNull(client, "client"));
    }

    public Templates templates(final SendByteClient client, final SendByteQuarkusConfig config) {
        requireEnabled(config.email(), "email");
        return new Templates(Objects.requireNonNull(client, "client"));
    }

    public Suppressions suppressions(
            final SendByteClient client, final SendByteQuarkusConfig config) {
        requireEnabled(config.email(), "email");
        return new Suppressions(Objects.requireNonNull(client, "client"));
    }

    public ApiKeys apiKeys(final SendByteClient client, final SendByteQuarkusConfig config) {
        requireEnabled(config.admin(), "admin");
        return new ApiKeys(Objects.requireNonNull(client, "client"));
    }

    public Marketing marketing(final SendByteClient client, final SendByteQuarkusConfig config) {
        requireEnabled(config.marketing(), "marketing");
        return new Marketing(Objects.requireNonNull(client, "client"));
    }

    public Webhooks webhooks(final SendByteClient client, final SendByteQuarkusConfig config) {
        requireEnabled(config.webhooks(), "webhooks");
        return new Webhooks(Objects.requireNonNull(client, "client"));
    }

    private static void requireEnabled(
            final SendByteQuarkusConfig.Product product, final String name) {
        Objects.requireNonNull(product, "product");
        if (!product.enabled()) {
            throw new IllegalStateException("quarkus.sendbyte." + name + ".enabled is false");
        }
    }
}

package africa.sendbyte.quarkus.deployment;

import java.util.List;

/** Dependency-light metadata for the SendByte Quarkus extension package. */
public final class SendByteQuarkusExtensionMetadata {
    public static final String FEATURE = "sendbyte";
    public static final String RUNTIME_ARTIFACT = "africa.sendbyte:sendbyte-quarkus-runtime";
    public static final String DEPLOYMENT_ARTIFACT = "africa.sendbyte:sendbyte-quarkus-deployment";

    private static final List<String> PRODUCT_TYPES =
            List.of(
                    "africa.sendbyte.email.Emails",
                    "africa.sendbyte.email.Domains",
                    "africa.sendbyte.email.Templates",
                    "africa.sendbyte.email.Suppressions",
                    "africa.sendbyte.admin.ApiKeys",
                    "africa.sendbyte.marketing.Marketing",
                    "africa.sendbyte.webhooks.Webhooks");

    private SendByteQuarkusExtensionMetadata() {}

    public static List<String> productTypes() {
        return PRODUCT_TYPES;
    }
}

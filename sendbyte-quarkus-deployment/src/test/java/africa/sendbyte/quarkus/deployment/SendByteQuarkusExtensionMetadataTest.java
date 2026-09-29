package africa.sendbyte.quarkus.deployment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class SendByteQuarkusExtensionMetadataTest {
    @Test
    void exposesStableExtensionMetadataWithoutLoadingProductClasses() {
        assertEquals("sendbyte", SendByteQuarkusExtensionMetadata.FEATURE);
        assertEquals(
                "io.github.sendbyte-4j.sendbyte:sendbyte-quarkus-runtime",
                SendByteQuarkusExtensionMetadata.RUNTIME_ARTIFACT);
        assertEquals(
                "io.github.sendbyte-4j.sendbyte:sendbyte-quarkus-deployment",
                SendByteQuarkusExtensionMetadata.DEPLOYMENT_ARTIFACT);
        assertTrue(
                SendByteQuarkusExtensionMetadata.productTypes()
                        .contains("africa.sendbyte.email.Emails"));
        assertTrue(
                SendByteQuarkusExtensionMetadata.productTypes()
                        .contains("africa.sendbyte.webhooks.Webhooks"));
    }
}

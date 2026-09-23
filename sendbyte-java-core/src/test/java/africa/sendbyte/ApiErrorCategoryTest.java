package africa.sendbyte;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import africa.sendbyte.exception.ApiException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

final class ApiErrorCategoryTest {
    private static final Path OPENAPI_SNAPSHOT = Paths.get("..", "spec", "openapi.yaml");

    @Test
    void classifiesEveryErrorCodeInTheReviewedContract() throws IOException {
        final List<String> codes = contractErrorCodes();
        assertTrue(
                codes.contains("rate_limit_exceeded"),
                "read the wrong enum from " + OPENAPI_SNAPSHOT + ": " + codes);
        assertTrue(codes.size() > 6, "expected the full contract enum, read: " + codes);

        for (final String code : codes) {
            assertNotEquals(
                    ApiErrorCategory.UNKNOWN,
                    ApiErrorCategory.fromCode(code, 400),
                    "contract error code is not classified: " + code);
        }
    }

    @Test
    void fallsBackToTheStatusCodeWhenTheCodeIsMissingOrUnknown() {
        assertEquals(ApiErrorCategory.RATE_LIMIT, ApiErrorCategory.fromCode(null, 429));
        assertEquals(ApiErrorCategory.SERVER, ApiErrorCategory.fromCode("not_in_contract", 503));
        assertEquals(ApiErrorCategory.UNKNOWN, ApiErrorCategory.fromCode(null, 418));
    }

    @Test
    void exposesTheCategoryOnApiFailures() {
        assertEquals(
                ApiErrorCategory.PAYMENT,
                new ApiException("declined", 402, "payment_required", null, null).category());
        assertEquals(
                ApiErrorCategory.RECIPIENT,
                new ApiException("suppressed", 422, "suppressed_recipient", null, null).category());
        assertEquals(
                ApiErrorCategory.SERVER,
                new ApiException("unavailable", 503, null, null, null).category());
    }

    /**
     * Reads the error-code enum from the reviewed OpenAPI snapshot rather than a transcribed list,
     * so a code added to the contract fails this test until it is classified.
     */
    private static List<String> contractErrorCodes() throws IOException {
        final List<String> lines = Files.readAllLines(OPENAPI_SNAPSHOT, StandardCharsets.UTF_8);

        for (int index = 0; index < lines.size(); index++) {
            if (!"code:".equals(lines.get(index).trim())) {
                continue;
            }

            final int enumIndex = enumLineAfter(lines, index);
            if (enumIndex < 0) {
                continue;
            }

            final List<String> codes = new ArrayList<>();
            for (int entry = enumIndex + 1; entry < lines.size(); entry++) {
                final String trimmed = lines.get(entry).trim();
                if (!trimmed.startsWith("- ")) {
                    break;
                }
                codes.add(trimmed.substring(2).trim());
            }
            if (!codes.isEmpty()) {
                return codes;
            }
        }

        throw new IllegalStateException(
                "Could not find the error-code enum in " + OPENAPI_SNAPSHOT.toAbsolutePath());
    }

    private static int enumLineAfter(final List<String> lines, final int from) {
        for (int index = from + 1; index < Math.min(lines.size(), from + 8); index++) {
            if ("enum:".equals(lines.get(index).trim())) {
                return index;
            }
        }
        return -1;
    }

    @Test
    void contractSnapshotIsReachableFromTheModule() {
        assertFalse(
                Files.notExists(OPENAPI_SNAPSHOT), OPENAPI_SNAPSHOT.toAbsolutePath().toString());
    }
}

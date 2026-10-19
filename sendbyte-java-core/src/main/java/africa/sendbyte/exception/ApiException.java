package africa.sendbyte.exception;

import africa.sendbyte.ApiErrorCategory;
import africa.sendbyte.RequestDiagnostics;

/** A non-success response returned by the SendByte API. */
public class ApiException extends SendByteException {
    private static final long serialVersionUID = 1L;
    private final int statusCode;
    private final String code;
    private final String documentationUrl;
    private final String requestId;
    private final ApiErrorCategory category;

    public ApiException(
            final String message,
            final int statusCode,
            final String code,
            final String documentationUrl,
            final String requestId) {
        super(message);
        this.statusCode = statusCode;
        this.code = code;
        this.documentationUrl = documentationUrl;
        this.requestId = requestId;
        this.category = ApiErrorCategory.fromCode(code, statusCode);
    }

    public ApiException(
            final String message,
            final int statusCode,
            final String code,
            final String documentationUrl,
            final String requestId,
            final RequestDiagnostics diagnostics) {
        super(message, diagnostics);
        this.statusCode = statusCode;
        this.code = code;
        this.documentationUrl = documentationUrl;
        this.requestId = requestId;
        this.category = ApiErrorCategory.fromCode(code, statusCode);
    }

    public int statusCode() {
        return statusCode;
    }

    public String code() {
        return code;
    }

    public String documentationUrl() {
        return documentationUrl;
    }

    public String requestId() {
        return requestId;
    }

    /**
     * Classified failure meaning, resolved from {@link #code()} with the status code as a fallback.
     */
    public ApiErrorCategory category() {
        return category;
    }
}

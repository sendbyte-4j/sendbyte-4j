package africa.sendbyte.exception;

/** A non-success response returned by the SendByte API. */
public class ApiException extends SendByteException {
    private static final long serialVersionUID = 1L;
    private final int statusCode;
    private final String code;
    private final String documentationUrl;
    private final String requestId;

    public ApiException(final String message, final int statusCode, final String code,
                        final String documentationUrl, final String requestId) {
        super(message);
        this.statusCode = statusCode;
        this.code = code;
        this.documentationUrl = documentationUrl;
        this.requestId = requestId;
    }

    public int statusCode() {
        return this.statusCode;
    }

    public String code() {
        return this.code;
    }

    public String documentationUrl() {
        return this.documentationUrl;
    }

    public String requestId() {
        return this.requestId;
    }
}

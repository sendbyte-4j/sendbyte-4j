package africa.sendbyte.exception;

/** The request conflicts with existing state, including idempotency conflicts. */
public final class ConflictException extends ApiException {
    private static final long serialVersionUID = 1L;

    public ConflictException(final String message, final int statusCode, final String code,
                             final String documentationUrl, final String requestId) {
        super(message, statusCode, code, documentationUrl, requestId);
    }
}

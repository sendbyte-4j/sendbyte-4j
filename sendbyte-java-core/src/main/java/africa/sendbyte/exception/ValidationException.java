package africa.sendbyte.exception;

/** A request was rejected because one or more values were invalid. */
public final class ValidationException extends ApiException {
    private static final long serialVersionUID = 1L;

    public ValidationException(
            final String message,
            final int statusCode,
            final String code,
            final String documentationUrl,
            final String requestId) {
        super(message, statusCode, code, documentationUrl, requestId);
    }
}

package africa.sendbyte.exception;

/** The requested SendByte resource does not exist. */
public final class NotFoundException extends ApiException {
    private static final long serialVersionUID = 1L;

    public NotFoundException(final String message, final int statusCode, final String code,
                             final String documentationUrl, final String requestId) {
        super(message, statusCode, code, documentationUrl, requestId);
    }
}

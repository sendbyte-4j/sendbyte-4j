package africa.sendbyte.exception;

/** The current credentials do not permit the requested operation. */
public final class AuthorizationException extends ApiException {
    private static final long serialVersionUID = 1L;

    public AuthorizationException(final String message, final int statusCode, final String code,
                                  final String documentationUrl, final String requestId) {
        super(message, statusCode, code, documentationUrl, requestId);
    }
}

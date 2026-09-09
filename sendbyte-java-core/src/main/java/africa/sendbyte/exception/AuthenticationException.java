package africa.sendbyte.exception;

/** The supplied credentials were missing or invalid. */
public final class AuthenticationException extends ApiException {
    private static final long serialVersionUID = 1L;

    public AuthenticationException(final String message, final int statusCode, final String code,
                                   final String documentationUrl, final String requestId) {
        super(message, statusCode, code, documentationUrl, requestId);
    }
}

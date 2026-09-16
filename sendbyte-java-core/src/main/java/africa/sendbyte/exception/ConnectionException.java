package africa.sendbyte.exception;

import africa.sendbyte.RequestDiagnostics;

/** A request failed before the API returned a response. */
public final class ConnectionException extends SendByteException {
    private static final long serialVersionUID = 1L;

    public ConnectionException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ConnectionException(
            final String message, final Throwable cause, final RequestDiagnostics diagnostics) {
        super(message, cause, diagnostics);
    }
}

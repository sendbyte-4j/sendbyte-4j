package africa.sendbyte.exception;

import africa.sendbyte.RequestDiagnostics;

/** The response body exceeded the configured SDK buffer limit. */
public final class ResponseTooLargeException extends SendByteException {
    private static final long serialVersionUID = 1L;

    public ResponseTooLargeException(final String message, final RequestDiagnostics diagnostics) {
        super(message, diagnostics);
    }
}
